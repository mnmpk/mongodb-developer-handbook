
export enum Implementation {
    REPO = "Spring Data repositiory",
    SPRING = "Spring Data Mongo",
    DRIVER = "MongoDB native driver"
}
export enum WorkloadType {
    READ = "Read",
    WRITE = "Write"
}
export enum OperationType {
    INSERT = "Insert",
    UPDATE = "Update",
    DELETE = "Delete",
    REPLACE = "Replace"
}
export enum WriteConcern{
    majority = "majority",
    w0 = "0",
    w1 = "1",
    w2 = "2",
    w3 = "3"
}
export enum Coventer{
    SPRING = "Spring converter",
    MONGODB = "MongoDB Codec",
}
export class Workload {
    impl!:Implementation;
    type!: WorkloadType;
    coll!: string;
    schema!: string;
    converter!:Coventer;
    //TODO:Read options?

    //Write Option
    opType!:OperationType;
    numWorkers!:number;
    qty!:number;
    w!:WriteConcern|number;
    bulk!:boolean;

}