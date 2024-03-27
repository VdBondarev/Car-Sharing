package car.sharing.telegram.strategy.response;

public interface AdminResponseService {
    int ONE = 1;
    String COLON = ":";

    String getMessage(String text);

    boolean isApplicable(String text);

    default Long getId(String text) {
        return Long.valueOf(
                text.substring(
                        text.indexOf(COLON) + ONE).trim()
        );
    }
}
