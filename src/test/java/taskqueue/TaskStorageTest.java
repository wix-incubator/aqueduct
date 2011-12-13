package taskqueue;

import taskqueue.TaskStorage;

import java.util.UUID;

/**
 * Created by IntelliJ IDEA.
 * User: evg
 * Date: 21/11/11
 * Time: 00:04
 */
public class TaskStorageTest {

    private TaskStorage taskStorage = new TaskStorage(UUID.randomUUID().toString() + ".db");

}
