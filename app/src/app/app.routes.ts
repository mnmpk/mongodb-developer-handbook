import { Routes } from '@angular/router';
import { ChangeStreamComponent } from './main/change-stream/change-stream.component';
import { SearchComponent } from './main/search/search.component';
import { TransactionComponent } from './main/transaction/transaction.component';
import { WorkloadsComponent } from './main/workloads/workloads.component';

export const routes: Routes = [
    {
      path: '',
      children: []
    },
    { path: 'workloads', component: WorkloadsComponent },
    { path: 'transaction', component: TransactionComponent },
    { path: 'change-stream', component: ChangeStreamComponent },
    { path: 'search', component: SearchComponent },
];
