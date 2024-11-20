import { Routes } from '@angular/router';
import { ChangeStreamComponent } from './main/change-stream/change-stream.component';
import { SearchComponent } from './main/search/search.component';
import { TransactionComponent } from './main/transaction/transaction.component';
import { WorkloadsComponent } from './main/workloads/workloads.component';
import { DashboardComponent } from './main/dashboard/dashboard.component';

export const routes: Routes = [
    { path: 'dashboard', component: DashboardComponent },
    { path: 'workloads', component: WorkloadsComponent },
    { path: 'transaction', component: TransactionComponent },
    { path: 'change-stream', component: ChangeStreamComponent },
    { path: 'search', component: SearchComponent },
    { path: '', redirectTo: 'dashboard', pathMatch: 'prefix' },
];
