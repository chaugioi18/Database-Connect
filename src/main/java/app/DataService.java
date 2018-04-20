package app;

import com.google.inject.Inject;
import database.mysql.model.tables.records.TestdataRecord;
import database.mysql.repository.ITestDataRepo;
import org.springframework.transaction.annotation.Transactional;

public class DataService implements IDataService {

    private ITestDataRepo testDataRepo;

    @Inject
    DataService(ITestDataRepo testDataRepo) {
        this.testDataRepo = testDataRepo;
    }

    @Override
    public IDataService handleData(Data data) {
        TestdataRecord record = new TestdataRecord();
        record.setTime(data.getTime());
        record.setName(data.getName());
        executeSave(record);
        return this;
    }

    @Transactional
    @Override
    public void executeSave(TestdataRecord record) {
        testDataRepo.save(record);
    }
}
