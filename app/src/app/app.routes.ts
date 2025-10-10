import { Routes } from '@angular/router';
import { ChangeStreamComponent } from './main/change-stream/change-stream.component';
import { TransactionComponent } from './main/transaction/transaction.component';
import { WorkloadsComponent } from './main/workloads/workloads.component';
import { DashboardComponent } from './main/dashboard/dashboard.component';
import { GeoSpatialComponent } from './main/geo-spatial/geo-spatial.component';
import { CacheComponent } from './main/cache/cache.component';
import { SessionComponent } from './main/session/session.component';
import { ThreadComponent } from './main/thread/thread.component';

export const routes: Routes = [
    { path: 'dashboard', component: DashboardComponent },
    { path: 'workloads', component: WorkloadsComponent },
    { path: 'transaction', component: TransactionComponent },
    { path: 'change-stream', component: ChangeStreamComponent },
    { path: 'geo-spatial', component: GeoSpatialComponent },
    { path: 'cache', component: CacheComponent },
    { path: 'session', component: SessionComponent },
    { path: 'thread', component: ThreadComponent },
    { path: '', redirectTo: 'dashboard', pathMatch: 'prefix' },
];
