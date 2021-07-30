import { Component, OnInit } from '@angular/core';
import { SmeService } from 'src/app/services/sme.service';
import { ActivatedRoute, Router } from '@angular/router';
import { ITragetSystem } from 'src/app/interface/ITragetSystem';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-update',
  templateUrl: './update.component.html',
  styleUrls: ['./update.component.scss']
})
export class UpdateComponent implements OnInit {

  public id;
  public ts:ITragetSystem = null;

  constructor(public smeService: SmeService, private route: ActivatedRoute, private router: Router, private snackBar: MatSnackBar) {
    this.id = this.route.snapshot.params.id;
  }

  ngOnInit(): void {
    this.smeService.getTargetSystem(this.id)
      .then(ts => {
        this.ts = ts;
      })
      .catch(err => {
        this.router.navigate(['home']);
      })
  }

  openSnackBar(message: string, action: string, durationInSeconds: number) {
    let snackBarconfig = {
      duration: durationInSeconds * 1000
    }
    this.snackBar.open(message, action, snackBarconfig);
  }

}
