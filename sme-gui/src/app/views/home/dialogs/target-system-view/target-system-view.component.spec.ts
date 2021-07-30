import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TargetSystemViewComponent } from './target-system-view.component';

describe('TargetSystemViewComponent', () => {
  let component: TargetSystemViewComponent;
  let fixture: ComponentFixture<TargetSystemViewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TargetSystemViewComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TargetSystemViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
