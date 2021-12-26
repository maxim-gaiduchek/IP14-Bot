package main;

import datasource.DatasourceConfig;
import datasource.services.DBService;
import entities.Lecture;
import entities.Queue;
import entities.User;
import entities.enums.Discipline;
import entities.enums.LectureCount;
import entities.enums.WeekCount;
import entities.enums.WeekDay;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import utils.SimpleSender;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main extends TelegramLongPollingBot {

    private static final Long CHAT_ID = -1001598116577L;
    private static final String BOT_USERNAME = System.getenv("BOT_USERNAME");
    private static final String BOT_TOKEN = System.getenv("BOT_TOKEN");
    /*private static final String BOT_USERNAME = System.getenv("TEST_BOT_TELEGRAM_USERNAME");
    private static final String BOT_TOKEN = System.getenv("TEST_BOT_TELEGRAM_TOKEN");*/
    private final SimpleSender sender = new SimpleSender(BOT_TOKEN);

    public static final ApplicationContext CONTEXT = new AnnotationConfigApplicationContext(DatasourceConfig.class);
    private final DBService service = (DBService) CONTEXT.getBean("service");

    private static final DateFormat FORMAT_TIME = new SimpleDateFormat("HH:mm");
    private static final DateFormat FORMAT_DAY = new SimpleDateFormat("dd.MM");
    private static final DateFormat FORMAT_DATE = new SimpleDateFormat("dd.MM.yyyy");

    static {
        FORMAT_TIME.setTimeZone(TimeZone.getTimeZone("Europe/Kiev"));
        FORMAT_DAY.setTimeZone(TimeZone.getTimeZone("Europe/Kiev"));
        FORMAT_DATE.setTimeZone(TimeZone.getTimeZone("Europe/Kiev"));
    }

    private Main() {
        System.out.println(FORMAT_DATE.format(new Date()));
        System.out.println(FORMAT_TIME.format(new Date()));
        System.out.println(WeekCount.getCurrentWeekCount());
        System.out.println(WeekDay.getCurrentWeekDay());

        new Executor().start();
    }

    // parsing

    @Override
    public void onUpdateReceived(Update update) {
        System.out.println(update);

        if (update.hasMessage()) {
            parseMessage(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            parseCallbackQuery(update.getCallbackQuery());
        }
    }

    // message parsing

    private void parseMessage(Message message) {
        if (!CHAT_ID.equals(message.getChatId()) && !message.isUserMessage()) {
            sender.leaveChat(message.getChatId());
        }

        if (message.isCommand()) {
            parseCommand(message);
        } else if (message.hasText()) {
            parseText(message);
        }
    }

    private void parseCommand(Message message) {
        String text = message.getText();
        Long chatId = message.getChatId();

        switch (text) {
            case "/start", "/start@ip_14_bot", "/help", "/help@ip_14_bot" -> sendHelp(chatId);

            case "/today", "/today@ip_14_bot" -> sendSchedule(chatId);
            case "/lecture", "/lecture@ip_14_bot" -> sendCurrentLectureInfo(chatId);
            case "/next_day", "/next_day@ip_14_bot" -> sendNextDaySchedule(chatId);

            case "/monday", "/monday@ip_14_bot" -> sendWeekDaySchedule(WeekDay.MONDAY, chatId);
            case "/tuesday", "/tuesday@ip_14_bot" -> sendWeekDaySchedule(WeekDay.TUESDAY, chatId);
            case "/wednesday", "/wednesday@ip_14_bot" -> sendWeekDaySchedule(WeekDay.WEDNESDAY, chatId);
            case "/thursday", "/thursday@ip_14_bot" -> sendWeekDaySchedule(WeekDay.THURSDAY, chatId);
            case "/friday", "/friday@ip_14_bot" -> sendWeekDaySchedule(WeekDay.FRIDAY, chatId);
            // case "/saturday", "/saturday@ip_14_bot" -> sendWeekDaySchedule(WeekDay.SATURDAY, chatId);

            case "/minutes_left", "/minutes_left@ip_14_bot" -> sendMinutesLeft(chatId);
            // case "/mom", "/mom@ip_14_bot" -> mentionMoms(chatId);
            case "/lead", "/lead@ip_14_bot" -> mentionLeads(chatId, message.getMessageId());
            case "/sanya_dz", "/sanya_dz@ip_14_bot" -> mentionSanya(chatId, message.getMessageId());

            case "/queue", "/queue@ip_14_bot" -> QueueController.sendDisciplineChoose(sender, message);
        }

        if (chatId.equals(505457346L)) {
            if (text.startsWith("/set ")) {
                QueueController.setInQueue(sender, chatId, text.substring(5));
            } else if (text.startsWith("/delete ")) {
                QueueController.removeFromQueue(sender, text.substring(8));
            }
        }
    }

    private void parseText(Message message) {
        String text = message.getText().toLowerCase();
        Long chatId = message.getChatId();

        if (text.contains("1000-7")) {
            deadInsideCounter(chatId);
        } else if (text.contains("я люблю тебя") && message.isUserMessage()) {
            sendLove(chatId);
        }

        // if (text.contains("@мамочки") || text.contains("@мама")) mentionMoms(chatId);
    }

    private void sendHelp(Long chatId) {
        String msg = """
                /start, /help - все команды
                /today - расписание на сегодня
                /lecture - текущая лекция
                /next\\_day - расписание на следующий день
                /monday, /tuesday, /wednesday, /thursday, /friday - расписание на пн-пт
                /minutes\\_left - сколько минут осталось до конца пары или начала новой
                /lead - призывает старост

                Бот также может отвечать в лс: @ip\\_14\\_bot"""; // /mom - призывает мамочек :З

        sender.sendString(chatId, msg);
    }

    private void sendMinutesLeft(Long chatId) {
        List<Lecture> lectureList = getTodayLectures();

        if (lectureList.isEmpty()) {
            sender.sendString(chatId, "Сегодня лекций нет");
            return;
        }

        try {
            Date now = FORMAT_TIME.parse(FORMAT_TIME.format(new Date()));

            for (Lecture lecture : lectureList) {
                LectureCount count = lecture.getLectureCount();
                Date start = FORMAT_TIME.parse(count.getStartTime()), end = FORMAT_TIME.parse(count.getEndTime());

                if (start.before(now) && end.after(now)) {
                    int minutes = (int) Math.floor((end.getTime() - now.getTime()) / (60.0 * 1000));

                    sender.sendString(chatId, "До конца пары осталось: " + minutes + " минут(ы)");
                    return;
                }
            }

            for (int i = lectureList.size() - 1; i >= 0; i--) {
                Lecture lecture = lectureList.get(i);
                Date end = FORMAT_TIME.parse(lecture.getLectureCount().getEndTime());

                if (end.before(now)) {
                    if (i == lectureList.size() - 1) {
                        sender.sendString(chatId, "Пары уже закончились");
                    } else {
                        Lecture nextLecture = lectureList.get(i + 1);
                        Date start = FORMAT_TIME.parse(nextLecture.getLectureCount().getStartTime());

                        int minutes = (int) Math.floor((start.getTime() - now.getTime()) / (60.0 * 1000));

                        sender.sendString(chatId, "До начала новой пары осталось: " + minutes + " минут(ы)");
                    }
                    return;
                }
            }

            sendLectureInfo(lectureList.get(0), "Первая пара:", chatId);
        } catch (ParseException ignored) {
        }
    }

    private void mentionMoms(Long chatId) {
        if (CHAT_ID.equals(chatId)) {
            sender.sendString(chatId, "@ostrich\\_alexey @Pavelperov @andrey\\_rand");
        }
    }

    private void mentionLeads(Long chatId, Integer messageId) {
        User tym = service.getUser(1893274358L), sasha = service.getUser(564720531L);

        sender.sendString(chatId, tym.getNameWithLink() + " " + sasha.getNameWithLink(), messageId);
    }

    private void mentionSanya(Long chatId, Integer messageId) {
        User sasha = service.getUser(564720531L);

        sender.sendString(chatId, sasha.getNameWithLink() + " ДЗ СКИНЬ ПО АНГЛУ", messageId);
    }

    private void deadInsideCounter(Long chatId) {
        String[] gifs = {
                "CgACAgIAAxkBAAMyYTidYQbdbN0sj5K3bENvUcfHFYQAAj4PAALZVshJ2VLdBycx3rsgBA",
                "CgACAgQAAxkBAAMzYTidz9kdbAmLBBx3KPXxJpD-hP0AAm8CAAL0-ZRSkouI48_Rcc0gBA",
                "CgACAgQAAxkBAAM4YTieppYSpAOWMqXmy-GDZDqB0FwAAnoCAAIIDiVRYRFfu24qKYcgBA"
        };
        StringBuilder sb = new StringBuilder();

        for (int i = 1000; i > 0; i -= 7) {
            sb.append(i).append("-7\n");
        }

        sender.sendString(chatId, sb.toString());
        sender.sendDocument(chatId, gifs[new Random().nextInt(gifs.length)]);
    }

    private void sendLove(Long chatId) {
        new Thread(() -> {
            try {
                String[] stickers = {
                        "CAACAgIAAxkBAAISs2HI7nwq1d5OKUY7OS6oUrz-8ZKGAALEEgACwHXwS3eA4YHGiwu-IwQ",
                        "CAACAgIAAxkBAAISsmHI7nxMKOQoxlFT46qUjRqMqB0DAAJAEwACBJ4hSHyf1cbaZtpyIwQ",
                        "CAACAgIAAxkBAAISsWHI7nwt0YX3PP3AdzpVd5Ze3Y6aAAJnDwAC2yjQSm9z_iesKpruIwQ",
                        "CAACAgIAAxkBAAISsGHI7nyM7K7QDx3Y3TwyDHGLpoZgAAIREQACsIPpSNFR-ddEeFsaIwQ",
                        "CAACAgIAAxkBAAISr2HI7nx-4ufiTDo617YX-vxhEQQ0AAKSAQACEBptIuuEW7yIvJNpIwQ"
                };

                sender.sendString(chatId, "а знаешь...");
                Thread.sleep(1000);
                sender.sendString(chatId, "я тоже.........................................");
                Thread.sleep(1000);
                sender.sendString(chatId, "ЛЮБЛЮ ТЕБЯ❤️❤️\uD83D\uDC96\uD83D\uDC97\uD83D\uDC97\uD83D\uDC9E\uD83D\uDC96\uD83D\uDC9E❤️❤️\uD83D\uDC93\uD83D\uDC9E\uD83D\uDC97\uD83D\uDC9A\uD83D\uDC97\uD83D\uDC95\uD83D\uDC9A\uD83D\uDC97\uD83D\uDC95\uD83D\uDC9B\uD83D\uDC9A\uD83D\uDC97\uD83D\uDC9A\uD83E\uDDE1\uD83D\uDC9E\uD83D\uDC99\uD83D\uDC97\uD83D\uDC9E\uD83D\uDC9E\uD83D\uDC97\uD83D\uDC95\uD83D\uDC96\uD83D\uDC9A\uD83D\uDC9A\uD83D\uDC96\uD83D\uDC95\uD83D\uDC97\uD83D\uDC9E\uD83D\uDC99\uD83D\uDC93❣️\uD83D\uDC95\uD83E\uDD0E\uD83D\uDC94\uD83D\uDC9F\uD83D\uDDA4\uD83D\uDC94\uD83D\uDC9A\uD83D\uDC98\uD83D\uDC9F\uD83D\uDC9C\uD83D\uDC97❣️\uD83D\uDC97\uD83D\uDDA4\uD83E\uDD0E\uD83D\uDC9E❣️\uD83D\uDC93\uD83D\uDC8C\uD83D\uDC9F\uD83D\uDC96");
                Thread.sleep(1000);
                for (int i = 0; i < 5; i++) {
                    sender.sendSticker(chatId, stickers[new Random().nextInt(stickers.length)]);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // callback parsing

    private void parseCallbackQuery(CallbackQuery callbackQuery) {
        Message message = callbackQuery.getMessage();
        Long chatId = message.getChatId();
        Integer messageId = message.getMessageId();

        String data = callbackQuery.getData();
        int index = data.indexOf('_');
        String query, text = null;

        if (index >= 0) {
            query = data.substring(0, index);
            text = data.substring(index + 1);
        } else {
            query = data;
        }

        switch (query) {
            case "queue-start" -> QueueController.sendDisciplineChoose(sender, chatId, messageId);
            case "queue" -> {
                if (message.isGroupMessage() || message.isSuperGroupMessage()) {
                    QueueController.sendLabNumberChoose(sender, chatId, messageId, text);
                } else {
                    QueueController.sendQueue(sender, chatId, messageId, text);
                }
            }

            case "choose-lab-num" -> QueueController.sendLabQueue(sender, chatId, messageId, text);

            case "add-lab" -> QueueController.sendLabNumberChoose(sender, chatId, messageId, text, true);
            case "add-lab-num" -> QueueController.addInQueue(sender, chatId, messageId, text);

            case "remove-lab" -> QueueController.sendLabNumberChoose(sender, chatId, messageId, text, false);
            case "remove-lab-num" -> QueueController.removeFromQueue(sender, chatId, messageId, text);
        }
    }

    // main execution

    private class Executor extends Thread {

        private Executor() {
        }

        @Override
        public void run() {
            long start;
            Date now;

            while (true) {
                start = System.currentTimeMillis();
                now = new Date();

                String time = FORMAT_TIME.format(now);

                switch (time) {
                    case "07:00" -> sendSchedule(CHAT_ID);
                    case "08:00" -> sendBirthday();
                    default -> sendOnLectureStartsOrEnds(time);
                }

                delay(60000, start);
            }
        }

        private void delay(int delay, long startExecutionTime) {
            try {
                sleep(delay - (System.currentTimeMillis() - startExecutionTime));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // lectures

    private void sendSchedule(Long chatId) {
        sendSchedule(WeekDay.getCurrentWeekDay(), WeekCount.getCurrentWeekCount(), chatId);
    }

    private void sendNextDaySchedule(Long chatId) {
        WeekDay now = WeekDay.getCurrentWeekDay();
        WeekCount count = WeekCount.getCurrentWeekCount();

        for (int i = now.getCount() + 1; i <= now.getCount() + 14; i++) {
            WeekDay day = WeekDay.getWeekDayByCounter(i);

            if (i == 8) count = count == WeekCount.FIRST ? WeekCount.SECOND : WeekCount.FIRST;

            if (!service.getAllLectures(day, count).isEmpty()) {
                sendSchedule(day, count, chatId);
                return;
            }
        }

        sender.sendString(chatId, "Лекций вообще нет");
    }

    private void sendWeekDaySchedule(WeekDay day, Long chatId) {
        WeekDay now = WeekDay.getCurrentWeekDay();
        WeekCount count = WeekCount.getCurrentWeekCount();

        for (int i = now.getCount() + 1; i <= now.getCount() + 14; i++) {
            if (i == 8) count = count == WeekCount.FIRST ? WeekCount.SECOND : WeekCount.FIRST;

            if (!service.getAllLectures(day, count).isEmpty()) {
                sendSchedule(day, count, chatId);
                return;
            }
        }
    }

    private void sendSchedule(WeekDay day, WeekCount count, Long chatId) {
        List<Lecture> lectureList = service.getAllLectures(day, count);

        if (lectureList.isEmpty()) {
            String msg = day == WeekDay.SUNDAY ?
                    "Оп оп, выходной, живем живем" : "Сегодня выходной. Чиллим, дамы и господа";

            sender.sendStringWithDisabledNotifying(chatId, msg);
            return;
        }

        long millisInDay = 24L * 60 * 60 * 1000;
        long diff = (day.getCount() - WeekDay.getCurrentWeekDay().getCount()) * millisInDay;

        if (count != WeekCount.getCurrentWeekCount()) {
            diff += 7 * millisInDay;
        }

        Date date = new Date(new Date().getTime() + diff);

        StringBuilder sb = new StringBuilder(day.getDayName())
                .append(", ")
                .append(FORMAT_DATE.format(date))
                .append(", ")
                .append(count == WeekCount.FIRST ? "Первая неделя" : "Вторая неделя");

        for (Lecture lecture : lectureList) {
            sb.append("\n\n").append(lecture.getLectureInfo());
        }

        sender.sendStringWithDisabledWebPagePreview(chatId, sb.toString());
    }

    private void sendOnLectureStartsOrEnds(String time) {
        List<Lecture> lectureList = getTodayLectures();

        for (int i = 0; i < lectureList.size(); i++) {
            Lecture lecture = lectureList.get(i);
            LectureCount count = lecture.getLectureCount();

            if (time.equals(count.getStartTime())) {
                sendLectureInfo(lecture, "Пара уже начинается:", CHAT_ID);
                return;
            } else if (time.equals(count.getEndTime())) {
                if (i == lectureList.size() - 1) {
                    sender.sendString(CHAT_ID, "Ура, пары завершились! Вот пары на следующий день");
                    sendNextDaySchedule(CHAT_ID);
                } else {
                    sendLectureInfo(lectureList.get(i + 1), "Пара завершилась. Следущая пара:", CHAT_ID);
                }
                return;
            }
        }

        // sendLectureInfo(lectureList.get(0), "Пара уже начинается:", CHAT_ID);
    }

    private void sendCurrentLectureInfo(Long chatId) {
        List<Lecture> lectureList = getTodayLectures();

        if (lectureList.isEmpty()) {
            sender.sendString(chatId, "Сегодня лекций нет");
            return;
        }

        try {
            Date now = FORMAT_TIME.parse(FORMAT_TIME.format(new Date()));

            for (Lecture lecture : lectureList) {
                LectureCount count = lecture.getLectureCount();
                Date start = FORMAT_TIME.parse(count.getStartTime()), end = FORMAT_TIME.parse(count.getEndTime());

                if (start.before(now) && end.after(now)) {
                    sendLectureInfo(lecture, "Текущая пара:", chatId);
                    return;
                }
            }

            for (int i = lectureList.size() - 1; i >= 0; i--) {
                Lecture lecture = lectureList.get(i);
                Date end = FORMAT_TIME.parse(lecture.getLectureCount().getEndTime());

                if (end.before(now)) {
                    if (i == lectureList.size() - 1) {
                        sender.sendString(chatId, "Пары уже закончились");
                    } else {
                        sendLectureInfo(lectureList.get(i + 1), "Следущая пара:", chatId);
                    }
                    return;
                }
            }

            sendLectureInfo(lectureList.get(0), "Первая пара:", chatId);
        } catch (ParseException ignored) {
        }
    }

    private List<Lecture> getTodayLectures() {
        return service.getAllLectures(WeekDay.getCurrentWeekDay(), WeekCount.getCurrentWeekCount());
    }

    private void sendLectureInfo(Lecture lecture, String startMsg, Long chatId) {
        String msg = startMsg + "\n" +
                "\n" +
                lecture.getLectureInfo();

        sender.sendStringWithDisabledWebPagePreview(chatId, msg);
    }

    // birthdays

    private void sendBirthday() {
        Date now = new Date();
        String date = FORMAT_DAY.format(now);
        int year = Integer.parseInt(FORMAT_DATE.format(now).substring(6));

        for (User user : service.getUsersByBirthday(date)) {
            String msg = user.getNameWithLink() + " сегодня празднует свой *День рождения!* " +
                    "Ей (ему) исполняется *" + user.getAge(year) + "* годиков!";

            sender.sendString(CHAT_ID, msg);
            sender.sendString(CHAT_ID, user.getBirthdayCommand());
        }
    }

    // main

    @Override
    public String getBotUsername() {
        return BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    public static void main(String[] args) {
        try {
            System.out.println();

            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);

            telegramBotsApi.registerBot(new Main());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
