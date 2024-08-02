
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
export enum WriteConcern {
    ACKNOWLEDGED = "Acknowledge", 
    W1 = "1", 
    W2 = "2", 
    W3 = "3", 
    UNACKNOWLEDGED = "Unacknowledge", 
    JOURNALED = "Journaled", 
    MAJORITY = "Majority"
}
export enum Converter {
    SPRING = "Spring converter",
    MONGODB = "MongoDB Codec",
}
export class Workload {
    impl!: Implementation;
    type!: WorkloadType;
    coll!: string;
    entity!: string;
    schema!: string;
    converter!: Converter;
    //TODO:Read options?

    //Write Option
    opType!: OperationType;
    numWorkers!: number;
    qty!: number;
    w!: WriteConcern;
    bulk!: boolean;

}