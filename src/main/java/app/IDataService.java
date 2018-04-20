package app;

import database.mysql.model.tables.records.TestdataRecord;

public interface IDataService {

    IDataService handleData(Data data);

    void executeSave(TestdataRecord record);
}
