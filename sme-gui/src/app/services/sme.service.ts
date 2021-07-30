import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ToastrService } from 'ngx-toastr';
import { IFileUpload } from '../interface/IFileUpload';
import { IResponse } from '../interface/IResponse';
import { ITargetSystemGenerationRequest } from '../interface/ITargetSystemGenerationRequest';
import { ITragetSystem } from '../interface/ITragetSystem';
import { BackendResolverService } from './backend-resolver.service';

@Injectable({
  providedIn: 'root'
})
export class SmeService {

  private backendAddress = "";

  constructor(private http: HttpClient, private toastr: ToastrService, private backendResolver: BackendResolverService) {
    this.backendAddress = backendResolver.getBackendUrl();
  }

  getTargetSystem(id): Promise<ITragetSystem> {
    return new Promise<ITragetSystem>((resolve, reject) => {
      this.http.get(this.backendAddress + "/targetsystem/" + id).subscribe((ts: ITragetSystem) => {
        resolve(ts)
      }, err => {
        console.log(err);
        if (err.error.error != null || err.error.error != undefined) {
          this.toastr.error(err.error.error, "Error");
        }
        else {
          this.toastr.error(err.error.status, "Error");
        }
        reject(err);
      })
    })
  }

  getAllTargetSystems(): Promise<ITragetSystem[]> {
    return new Promise<ITragetSystem[]>((resolve, reject) => {
      this.http.get(this.backendAddress + "/targetsystem").subscribe((targetSystems: ITragetSystem[]) => {
        resolve(targetSystems)
      }, err => {
        reject([]);
      })
    })
  }

  uploadModel(model, targetSystemKey) {
    return new Promise((resolve, reject) => {
      let formData = new FormData();
      formData.append("file", model);
      formData.append("location", targetSystemKey);

      this.http.post(this.backendAddress + "/upload", formData).subscribe(
        (res: IFileUpload) => {
          resolve(res.location);
        },
        (err) => {
          this.toastr.error("Model upload failed", "Error")
          reject(err);
        }
      );
    })
  }

  createTargetSystem(targetSystemGenerateRequest: ITargetSystemGenerationRequest) {
    return new Promise((resolve, reject) => {
      this.http.post(this.backendAddress + "/targetsystem", targetSystemGenerateRequest).subscribe((res: IResponse) => {
        this.toastr.success(res.status, "Success");
        resolve(true);
      },
        err => {
          if (err.error.error != null || err.error.error != undefined) {
            this.toastr.error(err.error.error, "Error");
          }
          else {
            this.toastr.error(err.error.status, "Error");
          }
          reject(err);
        })
    })
  }

  deleteTargetSystem(id) {
    return new Promise((resolve, reject) => {
      this.http.delete(this.backendAddress + "/targetsystem/" + id).subscribe((res: IResponse) => {
        this.toastr.success(res.status, "Success");
        resolve(true);
      }, err => {
        if (err.error.error != null || err.error.error != undefined) {
          this.toastr.error(err.error.error, "Error");
        }
        else {
          this.toastr.error(err.error.status, "Error");
        }
        reject(err);
      })
    })
  }

  updateTargetSystem(targetSystem: ITragetSystem) {
    return new Promise((resolve, reject) => {
      this.http.put(this.backendAddress + "/targetsystem", targetSystem).subscribe((res: IResponse) => {
        this.toastr.success(res.status, "Success");
        resolve(true);
      }, err => {
        if (err.error.error != null || err.error.error != undefined) {
          this.toastr.error(err.error.error, "Error");
        }
        else {
          this.toastr.error(err.error.status, "Error");
        }
        reject(err);
      })
    })
  }
}