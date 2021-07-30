import { Component, Injectable } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { LoadingComponent } from './loading.component';

@Injectable({
  providedIn: 'root'
})
export class LoadingModalService {

  constructor(private dialog: MatDialog) { }

  private dialogRef;

  show() {
    this.dialogRef = this.dialog.open(LoadingComponent, {
      disableClose: true
    });
  }

  close() {
    this.dialogRef.close()
  }
}