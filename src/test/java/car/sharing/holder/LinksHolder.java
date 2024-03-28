package car.sharing.holder;

public class LinksHolder {
    protected static final String ADD_CARS_FILE_NAME =
            "classpath:database/insert-cars-to-cars-table.sql";
    protected static final String REMOVE_CARS_FILE_NAME =
            "classpath:database/remove-cars-from-cars-table.sql";
    protected static final String ADD_RENTALS_FILE_NAME =
            "classpath:database/insert-rentals-to-rentals-table.sql";
    protected static final String REMOVE_RENTALS_FILE_NAME =
            "classpath:database/remove-rentals-from-rentals-table.sql";

    protected static String substring(String fileName) {
        int index = fileName.indexOf(":");
        return fileName.substring(index + 1);
    }
}
