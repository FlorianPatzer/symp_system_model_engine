import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { ITragetSystem } from 'src/app/interface/ITragetSystem';
import { LoadingModalService } from 'src/app/services/loading-modal.service';
import { SmeService } from 'src/app/services/sme.service';
import { DeleteDialogComponent } from './dialogs/delete-dialog/delete-dialog.component';
import { TargetSystemViewComponent } from './dialogs/target-system-view/target-system-view.component';


@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss'],
})
export class HomeComponent implements OnInit {

  public tableData: ITragetSystem[];

  constructor(private smeService: SmeService, public dialog: MatDialog, private router: Router) { }

  ngOnInit(): void {
    this.reloadTableData();
  }

  reloadTableData() {
    this.smeService.getAllTargetSystems().then(tsData => {
      this.tableData = tsData;
    });
  }

  deleteTargetSystem(ts: ITragetSystem) {
    let dialogRef = this.dialog.open(DeleteDialogComponent, {
      data: ts,
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.smeService.deleteTargetSystem(ts.id).then(_ => {
          this.reloadTableData();
        })
      }
    });
  }

  editTargetSystem(ts: ITragetSystem) {
    this.router.navigate(['update/' + ts.id]);
  }

  viewTargetSystem(ts: ITragetSystem) {
    this.dialog.open(TargetSystemViewComponent, {
      data: ts,
      width: '40vw'
    });
  }

  displayedColumns: string[] = ['id', 'name', 'actions',];

}



