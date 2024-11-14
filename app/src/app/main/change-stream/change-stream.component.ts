import { Component, ViewChild } from '@angular/core';
import { MatSort, Sort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { Apollo, gql } from 'apollo-angular';
import { map, Observable, Subscription } from 'rxjs';
const gqlMap: any = {
  "accountArea15days": [gql`
    query accountArea15days {
      getAccountArea15days {
        _id
        type
        bucketSize
        acct
        areaCode
        sumBet
        sumCasinoWin
        sumTheorWin
        noOfTxn
        avgBet
        avgCasinoWin
        avgTheorWin
      }
    }`, gql`
    subscription accountArea15days {
      watchAccountArea15days {
        _id
        type
        bucketSize
        acct
        areaCode
        sumBet
        sumCasinoWin
        sumTheorWin
        noOfTxn
        avgBet
        avgCasinoWin
        avgTheorWin
      }
    }`],


  "accountArea3mins": [gql`
      query accountArea3mins {
        getAccountArea3mins {
          _id
          type
          bucketSize
          acct
          areaCode
          sumBet
          sumCasinoWin
          sumTheorWin
          noOfTxn
          avgBet
          avgCasinoWin
          avgTheorWin
        }
      }`, gql`
      subscription accountArea3mins {
        watchAccountArea3mins {
          _id
          type
          bucketSize
          acct
          areaCode
          sumBet
          sumCasinoWin
          sumTheorWin
          noOfTxn
          avgBet
          avgCasinoWin
          avgTheorWin
        }
      }`],
    
        
      "accountCasinoArea1day": [gql`
          query accountCasinoArea1day {
            getAccountCasinoArea1day {
              _id
              type
              bucketSize
              acct
              casinoCode
              areaCode
              sumBet
              sumCasinoWin
              sumTheorWin
              noOfTxn
              avgBet
              avgCasinoWin
              avgTheorWin
            }
          }`, gql`
          subscription accountCasinoArea1day {
            watchAccountCasinoArea1day {
              _id
              type
              bucketSize
              acct
              casinoCode
              areaCode
              sumBet
              sumCasinoWin
              sumTheorWin
              noOfTxn
              avgBet
              avgCasinoWin
              avgTheorWin
            }
          }`],


    
        
      "accountCasinoArea3mins": [gql`
        query accountCasinoArea3mins {
          getAccountCasinoArea3mins {
            _id
            type
            bucketSize
            acct
            casinoCode
            areaCode
            sumBet
            sumCasinoWin
            sumTheorWin
            noOfTxn
            avgBet
            avgCasinoWin
            avgTheorWin
          }
        }`, gql`
        subscription accountCasinoArea3mins {
          watchAccountCasinoArea3mins {
            _id
            type
            bucketSize
            acct
            casinoCode
            areaCode
            sumBet
            sumCasinoWin
            sumTheorWin
            noOfTxn
            avgBet
            avgCasinoWin
            avgTheorWin
          }
        }`],



        
      "accountCasino1day": [gql`
        query accountCasino1day {
          getAccountCasino1day {
            _id
            type
            bucketSize
            acct
            casinoCode
            sumBet
            sumCasinoWin
            sumTheorWin
            noOfTxn
            avgBet
            avgCasinoWin
            avgTheorWin
          }
        }`, gql`
        subscription accountCasino1day {
          watchAccountCasino1day {
            _id
            type
            bucketSize
            acct
            casinoCode
            sumBet
            sumCasinoWin
            sumTheorWin
            noOfTxn
            avgBet
            avgCasinoWin
            avgTheorWin
          }
        }`]
}

@Component({
  selector: 'app-change-stream',
  templateUrl: './change-stream.component.html',
  styleUrl: './change-stream.component.scss'
})
export class ChangeStreamComponent {
  @ViewChild(MatSort) sort!: MatSort;

  type = 'accountArea';
  duration = '15days';

  public query!: any;
  public queryResult!: Observable<any>;
  subscription!: Subscription;
  dataSource: MatTableDataSource<any> = new MatTableDataSource();
  displayedColumns: string[] = ['acct', 'casinoCode', 'areaCode', 'sumBet', 'avgBet'];

  constructor(private readonly apollo: Apollo) { }

  ngOnInit() {
    this.change();
  }
  ngAfterViewInit(){
    this.dataSource.sort = this.sort;
  }
  change() {
    if(this.subscription){
      this.subscription.unsubscribe();
    }
    const gql = gqlMap[this.type + this.duration];
    if (gql) {
      this.query = this.apollo
        .watchQuery({
          query: gql[0],
        });
      this.queryResult = this.query.valueChanges;
      this.subscription = this.queryResult.subscribe((result) => {
        this.dataSource.data = result.data[Object.keys(result.data)[0]];
      });
      this.query.subscribeToMore({
        document: gql[1],
        updateQuery: (prev: any, result: any) => {
          //console.log(result.subscriptionData.data[Object.keys(result.subscriptionData.data)[0]]);
        },
      });
    }
  }
}
