package bot.utils;

public class Utils {
    // Método para validar si un ID de usuario es válido
    public static boolean isValidUserId(String userId) {
        return userId != null && userId.matches("\\d{17,19}");
    }

    // Método para formatear un mensaje de penalización
    public static String formatPenaltyMessage(String userName, String action) {
        return String.format("El usuario %s ha sido %s.", userName, action);
    }

    // Método para convertir un tiempo en milisegundos a un formato legible
    public static String formatDuration(long duration) {
        long seconds = duration / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%d minutos y %d segundos", minutes, seconds);
    }
}