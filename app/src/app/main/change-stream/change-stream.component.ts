import { Component } from '@angular/core';
import { Apollo, gql } from 'apollo-angular';
import { map, Observable } from 'rxjs';

const getAccountArea = gql`
query accountArea {
  getAccountArea {
    _id
    type
    acct
    areaCode
    sumBet
    sumCasinoWin
    sumTheorWin
    noOfTxn
  }
}`;
const watchAccountArea = gql`
subscription accountArea {
  watchAccountArea {
    _id
    type
    acct
    areaCode
    sumBet
    sumCasinoWin
    sumTheorWin
    noOfTxn
  }
}`;

@Component({
  selector: 'app-change-stream',
  templateUrl: './change-stream.component.html',
  styleUrl: './change-stream.component.scss'
})
export class ChangeStreamComponent {
  public accountAreaQuery!: any;
  public accountArea!: Observable<any>;
  
  constructor(private readonly apollo: Apollo) {}

  ngOnInit() {
    this.accountAreaQuery = this.apollo
      .watchQuery({
        query: getAccountArea,
      });
      this.accountArea = this.accountAreaQuery.valueChanges;

      
      this.accountArea.subscribe((result) => {
          console.log(result.data.getAccountArea);
      });
      this.accountAreaQuery.subscribeToMore({
        document: watchAccountArea,
        updateQuery: (prev:any, result:any) => {
          console.log(result.subscriptionData.data.watchAccountArea);
        },
      });
      
  }
}
