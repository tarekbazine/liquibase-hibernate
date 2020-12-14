package liquibase.ext.hibernate.diff;

import liquibase.change.Change;
import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.database.Database;
import liquibase.diff.Difference;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.output.DiffOutputControl;
import liquibase.ext.hibernate.database.HibernateDatabase;
import liquibase.statement.DatabaseFunction;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;

import java.util.List;

/**
 * Hibernate and database types tend to look different even though they are not.
 * There are enough false positives that it works much better to suppress all column changes based on types.
 */
public class ChangedColumnChangeGenerator extends liquibase.diff.output.changelog.core.ChangedColumnChangeGenerator {

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Column.class.isAssignableFrom(objectType)) {
            return PRIORITY_ADDITIONAL;
        }
        return PRIORITY_NONE;
    }

    @Override
    protected void handleTypeDifferences(Column column, ObjectDifferences differences, DiffOutputControl control, List<Change> changes, Database referenceDatabase, Database comparisonDatabase) {

//      parameter.read_only_user=app_read
        //read_only_user

        System.out.println(LiquibaseConfiguration.getInstance().getProperty(GlobalConfiguration.class, "dddf").getValue());

        if (referenceDatabase instanceof HibernateDatabase || comparisonDatabase instanceof HibernateDatabase) {
            System.out.println(column + " getName: " + column.getName() + " getDifferences: " + differences.getDifferences() + " changes: " + changes);

            Difference difference = differences.getDifferences().iterator().next();


            System.out.println("getField: " + difference.getField());
            System.out.println("getComparedValue: " + difference.getComparedValue());
            System.out.println("getReferenceValue: " + difference.getReferenceValue());
//            if () {


            if (difference.getMessage().contains("'varchar(255)' to 'BIGINT(19)'")) {

                System.out.println("---------------");
                super.handleTypeDifferences(column, differences, control, changes, referenceDatabase, comparisonDatabase);
            }


            // do nothing, types tend to not match with hibernate
        } else {
            super.handleTypeDifferences(column, differences, control, changes, referenceDatabase, comparisonDatabase);
        }
    }


    @Override
    protected void handleDefaultValueDifferences(Column column, ObjectDifferences differences, DiffOutputControl control, List<Change> changes, Database referenceDatabase, Database comparisonDatabase) {
        if (referenceDatabase instanceof HibernateDatabase || comparisonDatabase instanceof HibernateDatabase) {
            Difference difference = differences.getDifference("defaultValue");
            if (difference != null) {
                if (difference.getReferenceValue() == null && difference.getComparedValue() instanceof DatabaseFunction) {
                    //database sometimes adds a function default value, like for timestamp columns
                    return;
                }
            }
            // do nothing, types tend to not match with hibernate
        }
        super.handleDefaultValueDifferences(column, differences, control, changes, referenceDatabase, comparisonDatabase);
    }
}
