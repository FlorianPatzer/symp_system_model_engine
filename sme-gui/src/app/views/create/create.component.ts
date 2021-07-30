import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { ToastrService } from 'ngx-toastr';
import { readAsText } from 'promise-file-reader';
import { CdkStepper } from '@angular/cdk/stepper';

import * as parser from 'xml-js';
import { HttpClient } from '@angular/common/http';
import { snakeCase } from "snake-case";
import { ITargetSystemGenerationRequest } from 'src/app/interface/ITargetSystemGenerationRequest';
import { SmeService } from 'src/app/services/sme.service';
import { Router } from '@angular/router';
import { LoadingModalService } from 'src/app/services/loading-modal.service';

@Component({
  selector: 'app-create',
  templateUrl: './create.component.html',
  styleUrls: ['./create.component.scss'],
  providers: [{ provide: CdkStepper }]
})
export class CreateComponent implements OnInit {

  phase1_models = [];
  phase2_model;
  phase2_model_name = ""
  outputModelName = "";
  targetSystemName = "";

  constructor(private toastr: ToastrService, private smeService: SmeService, private router: Router, public loadingModal: LoadingModalService) { }

  ngOnInit() {
    //this.phase1_models.push('');
  }

  appendModelsArray(e, i) {
    this.phase1_models[i] = e.target.files[0];
  }

  selectModel(e) {
    this.phase2_model = e.target.files[0];
    this.phase2_model_name = this.phase2_model.name;
    this.extractIdOfLastTask(this.phase2_model).then(output => {
      this.outputModelName = output;
    })
  }

  extractIdOfLastTask(bpmnFile) {
    return new Promise<string>((resolve, reject) => {

      readAsText(bpmnFile)
        .then(data => {
          let parsedBpmn = JSON.parse(parser.xml2json(data, { compact: true }));

          let bpmnProcess = parsedBpmn['bpmn:definitions']['bpmn:process'];
          let lastSequence = bpmnProcess['bpmn:endEvent']["bpmn:incoming"]['_text'];
          let serviceTasks = bpmnProcess["bpmn:serviceTask"];

          serviceTasks.forEach(task => {
            let taskOutgoingSeq = task['bpmn:outgoing'];
            if (taskOutgoingSeq != undefined || taskOutgoingSeq != null) {
              let taskOutgoingSeqName = taskOutgoingSeq['_text'];
              if (taskOutgoingSeqName == lastSequence) {
                resolve(task['_attributes']['id']);
              }
            }
          });

          reject(null);
        })
        .catch(err => {
          console.log(err);
          reject(null)
          this.toastr.error("Something went wrong when reading the file", 'Error');
        })
    });
  }

  addEntry() {
    this.phase1_models.push('')
  }

  trackByIndex(index: number, obj: any): any {
    return index;
  }

  async create() {
    this.loadingModal.show();
    let targetSystemKey = snakeCase(this.targetSystemName);

    let phase1_models_location = [];
    let phase2_model_location;

    // Upload phase 1 models
    this.phase1_models.forEach(async model => {
      let fileLocation;
      try {
        fileLocation = await this.smeService.uploadModel(model, targetSystemKey)
        phase1_models_location.push(fileLocation);
      }
      catch(e) {
        this.loadingModal.close();
        return;
      }
    });

    // Upload phase 2 model
    try{
      phase2_model_location = await this.smeService.uploadModel(this.phase2_model, targetSystemKey);
    }
    catch(e){
      this.loadingModal.close();
      return;
    }

    // Create db entry
    let targetSystemGenReq: ITargetSystemGenerationRequest = {
      name: this.targetSystemName,
      key: targetSystemKey,
      phase1_models_location: phase1_models_location,
      phase2_model_location: phase2_model_location
    }

    // Make a generation request to the backend
    this.smeService.createTargetSystem(targetSystemGenReq).then(_ => {
      this.loadingModal.close();
      this.router.navigate(['/home']);
    }).catch(err=>{
      this.loadingModal.close();
    })
  }
}