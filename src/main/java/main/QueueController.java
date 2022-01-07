package main;

import datasource.services.DBService;
import entities.Queue;
import entities.User;
import entities.enums.Discipline;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import utils.SimpleSender;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

public class QueueController {

    private static final DBService service = (DBService) Main.CONTEXT.getBean("service");

    private static final int MAX_QUEUES = 3;
    private static final int ROW_MAX_ELEMENTS = 3;

    private QueueController() {
    }

    // choose

    public static void sendDisciplineChoose(SimpleSender sender, Message message) {
        Long chatId = message.getChatId();

        if (message.isUserMessage() && service.isUserOfIP14(chatId)
                || message.isSuperGroupMessage() || message.isGroupMessage()) {
            sender.sendStringAndInlineKeyboard(chatId, "Выбери дисциплину", getDisciplineChooseKeyboard());
        }
    }

    public static void sendDisciplineChoose(SimpleSender sender, Long chatId, Integer messageId) {
        if (service.isUserOfIP14(chatId)) {
            sender.editMessageTextAndInlineKeyboard(chatId, messageId, "Выбери дисциплину", getDisciplineChooseKeyboard());
        }
    }

    public static void sendLabQueue(SimpleSender sender, Long chatId, Integer messageId, String text) {
        try {
            String[] split = text.split("-");
            Discipline discipline = Discipline.valueOf(split[0]);
            int labNumber = Integer.parseInt(split[1]);

            StringBuilder sb = new StringBuilder("*Очередь на ").append(discipline.getTitle())
                    .append(" (").append(labNumber).append(" лаба)*\n");
            List<Queue> top3List = service.getFirst3(discipline);
            List<Queue> queueList = service.getLabQueue(discipline, labNumber);

            if (!top3List.isEmpty()) {
                sb.append("\n*3 несгораемых места:*\n");

                for (Queue queue : top3List) {
                    User user = queue.getUser();

                    sb.append(queue.getQueueNumber())
                            .append(". [").append(user.getFormattedSurname()).append(" ").append(user.getFormattedName())
                            .append("](tg://user?id=").append(user.getChatId()).append(") *(")
                            .append(queue.getLabNumber()).append(" лаба)*\n");
                }
            }

            if (queueList.isEmpty()) {
                sb.append("\nОчередь на лабу пуста");
            } else {
                sb.append("\nОстальная очередь\n");

                for (Queue queue : queueList) {
                    User user = queue.getUser();

                    sb.append(queue.getQueueNumber())
                            .append(". [").append(user.getFormattedSurname()).append(" ").append(user.getFormattedName())
                            .append("](tg://user?id=").append(user.getChatId()).append(")").append("\n");
                }

                DateFormat format = new SimpleDateFormat("dd.MM.yyyy в HH:mm:ss");
                format.setTimeZone(TimeZone.getTimeZone("Europe/Kiev"));
                String date = format.format(new Date());

                sb.append("\n_Обновлено ").append(date).append("_");
            }

            sender.editMessageText(chatId, messageId, sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendQueue(SimpleSender sender, Long chatId, Integer messageId, String text) {
        try {
            sendQueue(sender, chatId, messageId, Discipline.valueOf(text));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void sendQueue(SimpleSender sender, Long chatId, Integer messageId, Discipline discipline) {
        StringBuilder sb = new StringBuilder("*Очередь на ").append(discipline.getTitle()).append("*\n\n");
        List<Queue> queueList = service.getFullQueue(discipline);

        List<Integer> userQueues = new ArrayList<>();

        if (queueList.isEmpty()) {
            sb.append("Очередь пуста");
        } else {
            for (Queue queue : queueList) {
                User user = queue.getUser();

                if (user.getChatId().equals(chatId)) {
                    sb.append(queue.getQueueNumber())
                            .append(". *").append(user.getFormattedSurname()).append(" ").append(user.getFormattedName())
                            .append("* (").append(queue.getLabNumber()).append(" лаба)").append(" _- Вы_\n");

                    userQueues.add(queue.getLabNumber());
                } else {
                    sb.append(queue.getQueueNumber())
                            .append(". [").append(user.getFormattedSurname()).append(" ").append(user.getFormattedName())
                            .append("](tg://user?id=").append(user.getChatId())
                            .append(") (").append(queue.getLabNumber()).append(" лаба)\n");
                }
            }

            if (userQueues.isEmpty()) {
                sb.append("\nВас еще нет в очереди!");
            } else {
                sb.append("\nВы заняли очередь на *лабы номер ")
                        .append(userQueues.stream().map(Object::toString).collect(Collectors.joining(", ")))
                        .append("*");
            }

            DateFormat format = new SimpleDateFormat("dd.MM.yyyy в HH:mm");
            format.setTimeZone(TimeZone.getTimeZone("Europe/Kiev"));
            String date = format.format(new Date());

            sb.append("\n_Обновлено ").append(date).append("_");
        }

        sender.editMessageTextAndInlineKeyboard(
                chatId, messageId, sb.toString(), getEnterOrLeaveQueueKeyboard(discipline, userQueues.size()));
    }

    public static void sendLabNumberChoose(SimpleSender sender, Long chatId, Integer messageId, String text) {
        try {
            Discipline discipline = Discipline.valueOf(text);
            List<Integer> numbers = new ArrayList<>();

            for (int i = 1; i <= discipline.getMaxLabs(); i++) {
                numbers.add(i);
            }

            List<List<InlineKeyboardButton>> keyboard = getLabNumberChooseKeyboard(numbers, discipline, "choose-lab-num");

            sender.editMessageTextAndInlineKeyboard(chatId, messageId, "Выбери номер лабы", keyboard);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendLabNumberChoose(SimpleSender sender, Long chatId, Integer messageId, String text, boolean toAdd) {
        try {
            Discipline discipline = Discipline.valueOf(text);
            User user = service.getUser(chatId);
            List<List<InlineKeyboardButton>> keyboard = toAdd ?
                    getAddLabNumberKeyboard(discipline, user) :
                    getRemoveLabNumberKeyboard(discipline, user);

            sender.editMessageTextAndInlineKeyboard(chatId, messageId, "Выбери номер лабы", keyboard);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addInQueue(SimpleSender sender, Long chatId, Integer messageId, String text) {
        try {
            String[] split = text.split("-");
            Discipline discipline = Discipline.valueOf(split[0]);
            int labNumber = Integer.parseInt(split[1]);
            User user = service.getUser(chatId);

            if (!service.isQueueHasUser(user, discipline, labNumber)
                    && service.countUserQueues(user, discipline, labNumber) < MAX_QUEUES) {
                int countQueue = service.countQueue(discipline);
                int queueNumber;

                if (countQueue >= 3) {
                    Queue last = service.getLastQueue(discipline, labNumber);

                    queueNumber = last != null ? (last.getQueueNumber() + 1) : 4;
                } else {
                    queueNumber = countQueue + 1;
                }

                service.addQueue(new Queue(user, discipline, labNumber, queueNumber));
            }

            sendQueue(sender, chatId, messageId, discipline);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void removeFromQueue(SimpleSender sender, Long chatId, Integer messageId, String text) {
        try {
            String[] split = text.split("-");
            Discipline discipline = Discipline.valueOf(split[0]);
            int labNumber = Integer.parseInt(split[1]);

            removeFromQueue(sender, chatId, discipline, labNumber);
            sendQueue(sender, chatId, messageId, discipline);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void removeFromQueue(SimpleSender sender, String text) {
        try {
            String[] split = text.split(" ");
            Long chatId = Long.parseLong(split[0]);
            Discipline discipline = Discipline.valueOf(split[1]);
            int labNumber = Integer.parseInt(split[2]);

            removeFromQueue(sender, chatId, discipline, labNumber);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void removeFromQueue(SimpleSender sender, Long chatId, Discipline discipline, int labNumber) {
        Queue userQueue = service.getUserQuery(service.getUser(chatId), discipline, labNumber);

        if (userQueue != null) {
            int queueNumber = userQueue.getQueueNumber();

            service.removeQueue(userQueue);

            if (queueNumber <= 3) {
                List<Queue> first3 = service.getFirst3(discipline);

                for (int i = 0; i < first3.size(); i++) {
                    Queue queue = first3.get(i);
                    Long userId = queue.getUser().getChatId();
                    String title = discipline.getTitle();

                    if (i == 0) {
                        String msg = "*Сейчас твоя очередь сдавать лабу по " + title + "!*";

                        for (int j = 0; j < 3; j++) sender.sendString(userId, msg);
                    } else if (i == 1) {
                        User upper = first3.get(0).getUser();
                        String msg = "Ты на *2 месте* в очереди по *" + title + "*. " +
                                "Ты будешь сдавать следующим, после " + upper.getNameWithLink() + ". Готовся!";

                        sender.sendString(userId, msg);
                    } else if (i == 2) {
                        User upper = first3.get(1).getUser();
                        String msg = "Ты на *3 месте* в очереди по *" + title + "* после " + upper.getNameWithLink() + ". " +
                                "Ты будешь сдавать лабу в ближайшее время!";

                        sender.sendString(userId, msg);
                    }
                }
            }
        }
    }

    public static void setInQueue(SimpleSender sender, Long chatId, String text) {
        try {
            String[] split = text.split(" ");

            User user = service.getUser(Long.parseLong(split[0]));

            if (user != null) {
                Discipline discipline = Discipline.valueOf(split[1]);
                int labNumber = Integer.parseInt(split[2]);
                int queueNumber = Integer.parseInt(split[3]);

                if (service.isQueueHasUser(user, discipline, labNumber)) {
                    service.removeQueue(service.getUserQuery(user, discipline, labNumber));
                }

                Queue queue = new Queue(user, discipline, labNumber, queueNumber);

                service.addQueue(queue);
                sender.sendString(chatId, queue.toString());
            } else {
                sender.sendString(chatId, "user == null");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // keyboards

    private static List<List<InlineKeyboardButton>> getDisciplineChooseKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        for (Discipline discipline : Discipline.values()) {
            row.add(InlineKeyboardButton.builder().text(discipline.getTitle()).callbackData("queue_" + discipline).build());

            if (row.size() == ROW_MAX_ELEMENTS) {
                keyboard.add(row);
                row = new ArrayList<>();
            }
        }

        if (!row.isEmpty()) {
            keyboard.add(row);
        }

        return keyboard;
    }

    private static List<List<InlineKeyboardButton>> getEnterOrLeaveQueueKeyboard(Discipline discipline, int userInQueues) {
        String title = discipline.toString();

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> updateRow = new ArrayList<>();
        List<InlineKeyboardButton> backRow = new ArrayList<>();

        updateRow.add(InlineKeyboardButton.builder().text("Обновить").callbackData("queue_" + discipline).build());
        backRow.add(InlineKeyboardButton.builder().text("<< Назад").callbackData("queue-start").build());

        keyboard.add(updateRow);
        keyboard.add(backRow);

        if (userInQueues < MAX_QUEUES) {
            List<InlineKeyboardButton> enterRow = new ArrayList<>();

            enterRow.add(InlineKeyboardButton.builder().text("Встать в очередь").callbackData("add-lab_" + title).build());
            keyboard.add(enterRow);
        }

        if (userInQueues > 0) {
            List<InlineKeyboardButton> exitRow = new ArrayList<>();

            exitRow.add(InlineKeyboardButton.builder().text("Выйти из очереди").callbackData("remove-lab_" + title).build());
            keyboard.add(exitRow);
        }

        return keyboard;
    }

    private static List<List<InlineKeyboardButton>> getAddLabNumberKeyboard(Discipline discipline, User user) {
        List<Integer> labNumbers = new ArrayList<>();
        List<Integer> queuedLabNumbers = service.getAllUserQueuedLabNumbers(user, discipline);

        for (int i = 1; i <= discipline.getMaxLabs(); i++) {
            if (!queuedLabNumbers.contains(i)) {
                labNumbers.add(i);
            }
        }

        return getLabNumberChooseKeyboard(labNumbers, discipline, "add-lab-num");
    }

    private static List<List<InlineKeyboardButton>> getRemoveLabNumberKeyboard(Discipline discipline, User user) {
        return getLabNumberChooseKeyboard(service.getAllUserQueuedLabNumbers(user, discipline), discipline, "remove-lab-num");
    }

    private static List<List<InlineKeyboardButton>> getLabNumberChooseKeyboard(List<Integer> numbers, Discipline discipline, String query) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> backRow = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        backRow.add(InlineKeyboardButton.builder().text("<< Назад").callbackData("queue_" + discipline).build());
        keyboard.add(backRow);

        for (int i : numbers) {
            row.add(InlineKeyboardButton.builder().text(Integer.toString(i)).callbackData(query + "_" + discipline + "-" + i).build());

            if (row.size() == ROW_MAX_ELEMENTS) {
                keyboard.add(row);
                row = new ArrayList<>();
            }
        }

        if (!row.isEmpty()) {
            keyboard.add(row);
        }

        return keyboard;
    }
}
