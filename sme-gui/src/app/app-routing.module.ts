import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { CreateComponent } from './views/create/create.component';
import { HomeComponent } from './views/home/home.component';
import { UpdateComponent } from './views/update/update.component';

const routes: Routes = [
  { path: 'create', component: CreateComponent },
  { path: 'home', component: HomeComponent },
  { path: 'update/:id', component: UpdateComponent },
  { path: '', redirectTo: 'home', pathMatch: 'full' },
  { path: '**', redirectTo: 'home' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { useHash: true })],
  exports: [RouterModule]
})
export class AppRoutingModule { }
