import { Inject } from '@angular/core';
import { Component, OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { ITragetSystem } from 'src/app/interface/ITragetSystem';

@Component({
  selector: 'app-target-system-view',
  templateUrl: './target-system-view.component.html',
  styleUrls: ['./target-system-view.component.scss']
})
export class TargetSystemViewComponent implements OnInit {

  constructor(public dialogRef: MatDialogRef<TargetSystemViewComponent>, @Inject(MAT_DIALOG_DATA) public ts: ITragetSystem, private router: Router) { }

  ngOnInit(): void {
  }

  close(): void {
    this.dialogRef.close();
  }

  edit() {
    this.close();
    this.router.navigate(['update/' + this.ts.id]);
  }

}