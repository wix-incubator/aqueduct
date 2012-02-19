package logging;

import taskqueue.HttpTaskQueue;

import java.util.logging.Logger;

/**
 * Created by evg.
 * Date: 05/02/12
 * Time: 14:27
 */
public class LogWrapper {
    private static Logger logger = Logger.getLogger("com.wixpress.aqueduct");
    
    public static void error(String message, Object... args){

        error(String.format(message, args));
    }

    public static void error(String message){

        logger.severe(message);
    }


    public static void debug(String message, Object... args){

        debug(String.format(message, args));
    }

    public static void debug(String message){

        logger.finest(message);
    }

    public static void info(String message, Object... args){

        info(String.format(message, args));
    }

    public static void info(String message){

        logger.info(message);
    }
}
