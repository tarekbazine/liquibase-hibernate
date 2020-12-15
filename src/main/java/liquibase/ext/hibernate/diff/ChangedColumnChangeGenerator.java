package liquibase.ext.hibernate.diff;

import java.util.List;
import java.util.Optional;
import liquibase.change.Change;
import liquibase.database.Database;
import liquibase.diff.Difference;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.output.DiffOutputControl;
import liquibase.ext.hibernate.database.HibernateDatabase;
import liquibase.statement.DatabaseFunction;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;

/**
 * Hibernate and database types tend to look different even though they are not. There are enough
 * false positives that it works much better to suppress all column changes based on types.
 */
public class ChangedColumnChangeGenerator extends
    liquibase.diff.output.changelog.core.ChangedColumnChangeGenerator {

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Column.class.isAssignableFrom(objectType)) {
            return PRIORITY_ADDITIONAL;
        }
        return PRIORITY_NONE;
    }

    //    @Override
//    protected void handleTypeDifferences(Column column, ObjectDifferences differences, DiffOutputControl control, List<Change> changes, Database referenceDatabase, Database comparisonDatabase) {
//        if (referenceDatabase instanceof HibernateDatabase || comparisonDatabase instanceof HibernateDatabase) {
//            // do nothing, types tend to not match with hibernate
//        } else {
//            super.handleTypeDifferences(column, differences, control, changes, referenceDatabase, comparisonDatabase);
//        }
//    }
    @Override
    protected void handleTypeDifferences(Column column, ObjectDifferences differences,
        DiffOutputControl control, List<Change> changes, Database referenceDatabase,
        Database comparisonDatabase) {

//      parameter.read_only_user=app_read
        //read_only_user

//        System.out.println(
//            LiquibaseConfiguration.getInstance().getProperty(GlobalConfiguration.class, "dddf")
//                .getValue());

        if (referenceDatabase instanceof HibernateDatabase
            || comparisonDatabase instanceof HibernateDatabase) {
            System.out.println(
                column + " getName: " + column.getName() + " getDifferences: " + differences
                    .getDifferences() + " changes: " + changes);

            Optional<Difference> difference =
                differences.getDifferences().stream().filter(
                    diff -> diff.getField().toString().equals("type") &&
                        diff.getReferenceValue().toString().equals("varchar(255)") &&
                        diff.getComparedValue().toString().equals("BIGINT(19)")
                ).findFirst();

//            Difference difference0 =
//                differences.getDifferences().iterator().next();
            if (difference.isPresent()) {

                Difference difference0 = difference.get();

                System.out.println("getField: " + difference0.getField());
                System.out.println("getComparedValue: " + difference0.getComparedValue());
                System.out.println("getReferenceValue: " + difference0.getReferenceValue());

//            if (difference.getMessage().contains("'varchar(255)' to 'BIGINT(19)'")) {

                super
                    .handleTypeDifferences(column, differences, control, changes, referenceDatabase,
                        comparisonDatabase);
            }

            // do nothing, types tend to not match with hibernate
        } else {
            super.handleTypeDifferences(column, differences, control, changes, referenceDatabase,
                comparisonDatabase);
        }
    }

//    private boolean contains(){
//
//    }

    @Override
    protected void handleDefaultValueDifferences(Column column, ObjectDifferences differences,
        DiffOutputControl control, List<Change> changes, Database referenceDatabase,
        Database comparisonDatabase) {
        if (referenceDatabase instanceof HibernateDatabase
            || comparisonDatabase instanceof HibernateDatabase) {
            Difference difference = differences.getDifference("defaultValue");
            if (difference != null) {
                if (difference.getReferenceValue() == null && difference
                    .getComparedValue() instanceof DatabaseFunction) {
                    //database sometimes adds a function default value, like for timestamp columns
                    return;
                }
            }
            // do nothing, types tend to not match with hibernate
        }
        super
            .handleDefaultValueDifferences(column, differences, control, changes, referenceDatabase,
                comparisonDatabase);
    }
}
