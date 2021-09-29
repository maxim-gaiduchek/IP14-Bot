import entities.Lecture;
import entities.User;
import entities.enums.LectureCount;
import entities.enums.LectureType;
import entities.enums.WeekCount;
import entities.enums.WeekDay;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
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

    private static List<Lecture> lectures;
    private static List<User> users;

    private static final Long CHAT_ID = -1001598116577L;
    private static final String BOT_USERNAME = System.getenv("BOT_USERNAME");
    private static final String BOT_TOKEN = System.getenv("BOT_TOKEN");
    /*private static final String BOT_USERNAME = System.getenv("TEST_BOT_TELEGRAM_USERNAME");
    private static final String BOT_TOKEN = System.getenv("TEST_BOT_TELEGRAM_TOKEN");*/
    private final SimpleSender sender = new SimpleSender(BOT_TOKEN);

    private static final DateFormat FORMAT_TIME = new SimpleDateFormat("HH:mm");
    private static final DateFormat FORMAT_DAY = new SimpleDateFormat("dd.MM");
    private static final DateFormat FORMAT_DATE = new SimpleDateFormat("dd.MM.yyyy");

    static {
        FORMAT_TIME.setTimeZone(TimeZone.getTimeZone("GMT+3"));
        FORMAT_DATE.setTimeZone(TimeZone.getTimeZone("GMT+3"));
    }

    private Main() {
        setLectures();
        setUsers();

        System.out.println(FORMAT_DATE.format(new Date()));
        System.out.println(FORMAT_TIME.format(new Date()));
        System.out.println(WeekCount.getCurrentWeekCount());
        System.out.println(WeekDay.getCurrentWeekDay());

        new Executor().start();
    }

    // settings

    private void setLectures() {
        lectures = new ArrayList<>();

        // first week

        lectures.add(new Lecture(WeekDay.MONDAY, LectureCount.FIRST, WeekCount.FIRST, "Алгоритми та структури даних 1. Основи алгоритмізації", LectureType.LECTURE, "ст.вик. Вітковська І. І.", "302-18", "https://teams.microsoft.com/l/meetup-join/19%3Ad5119507f5ae4485bfc288df012ab877%40thread.tacv2/1630333406988?context=%7B%22Tid%22%3A%22d595e6a1-b90f-4cc6-b12b-1db7f331d222%22%2C%22Oid%22%3A%22aaa7729f-5f32-4c2a-b228-795e6c3fcb15%22%7D"));
        lectures.add(new Lecture(WeekDay.MONDAY, LectureCount.SECOND, WeekCount.FIRST, "Українська мова за професійним спрямуванням", LectureType.LECTURE, "доц. Кривенко С. М.", "302-18", "https://us06web.zoom.us/j/82224704701?pwd=UXg0RjVnQnZKUlZxZVNRMjJpUjlTQT09"));
        lectures.add(new Lecture(WeekDay.MONDAY, LectureCount.THIRD, WeekCount.FIRST, "Лінійна алгебра та аналітична геометрія", LectureType.LECTURE, "доц. Круглова Н. В.", "302-18", "https://us02web.zoom.us/j/84953634271?pwd=TzMzS2lNNUQyNUlHZW1uTStNbjA4QT09"));
        lectures.add(new Lecture(WeekDay.MONDAY, LectureCount.FORTH, WeekCount.FIRST, "Основи здорового способу життя (2-10)", LectureType.LECTURE, "доц. Міщук Д. М.", "302-18", "https://bbb.kpi.ua/b/69d-mff-dfw"));

        lectures.add(new Lecture(WeekDay.TUESDAY, LectureCount.FIRST, WeekCount.FIRST, "Екологічна безпека та цивільний захист", LectureType.PRACTICE, "ст.вик. Землянська О. В.", "102-22", "https://t.me/joinchat/S-36qYDMY9NjMGMy"));
        lectures.add(new Lecture(WeekDay.TUESDAY, LectureCount.SECOND, WeekCount.FIRST, "Іноземна мова 1. Практичний курс іноземної мови І", LectureType.PRACTICE, "вик. Бойко І. В.", "227-18", "https://us04web.zoom.us/j/74293924008?pwd=dDlEVDhVR3U1U1d3L3lkTXVqOXpmdz09"));
        lectures.add(new Lecture(WeekDay.TUESDAY, LectureCount.THIRD, WeekCount.FIRST, "Математичний аналіз 1. Диференціальне числення", LectureType.PRACTICE, "доц. Круглова Н. В.", "231-18", null));

        lectures.add(new Lecture(WeekDay.WEDNESDAY, LectureCount.SECOND, WeekCount.FIRST, "Основи здорового способу життя", LectureType.PRACTICE, "Міщук Діана Миколаївна", "231-18", null));
        lectures.add(new Lecture(WeekDay.WEDNESDAY, LectureCount.THIRD, WeekCount.FIRST, "Основи програмування 1. Базові конструкції", LectureType.PRACTICE, "Вітковська Ірина Іванівна", "200-18", null));

        lectures.add(new Lecture(WeekDay.THURSDAY, LectureCount.FIRST, WeekCount.FIRST, "Основи програмування 1. Базові конструкції", LectureType.LABORATORY, "Камінська Поліна Анатоліївна", "422-18", null));
        lectures.add(new Lecture(WeekDay.THURSDAY, LectureCount.SECOND, WeekCount.FIRST, "Алгоритми та структури даних 1. Основи алгоритмізації", LectureType.LABORATORY, "Мартинова Оксана Петрівна", "417-18", "https://teams.microsoft.com/l/meetup-join/19%3Ad5119507f5ae4485bfc288df012ab877%40thread.tacv2/1630333406988?context=%7B%22Tid%22%3A%22d595e6a1-b90f-4cc6-b12b-1db7f331d222%22%2C%22Oid%22%3A%22aaa7729f-5f32-4c2a-b228-795e6c3fcb15%22%7D"));
        lectures.add(new Lecture(WeekDay.THURSDAY, LectureCount.THIRD, WeekCount.FIRST, "Комп'ютерна дискретна математика", LectureType.PRACTICE, "Ліхоузова Тетяна Анатоліївна", "431-18", "https://t.me/joinchat/SWwPzWYpJ9dJsvCE"));

        lectures.add(new Lecture(WeekDay.FRIDAY, LectureCount.FIRST, WeekCount.FIRST, "Математичний аналіз 1. Диференціальне числення", LectureType.LECTURE, "Боднарчук Семен Володимирович", "303-18", "https://www.youtube.com/channel/UC2xqkl7Ic5BV5jGCVC8OznQ"));
        lectures.add(new Lecture(WeekDay.FRIDAY, LectureCount.SECOND, WeekCount.FIRST, "Основи програмування 1. Базові конструкції", LectureType.LECTURE, "Муха Ірина Павлівна", "303-18", "https://zoom.us/j/99775394017?pwd=aGhXMDlYZUd4K0h5aDVBbGdyZmY3QT09"));
        lectures.add(new Lecture(WeekDay.FRIDAY, LectureCount.THIRD, WeekCount.FIRST, "Комп'ютерна дискретна математика", LectureType.LECTURE, "Ліхоузова Тетяна Анатоліївна", "303-18", "https://t.me/joinchat/SWwPzWYpJ9dJsvCE"));

        // second week

        lectures.add(new Lecture(WeekDay.MONDAY, LectureCount.FIRST, WeekCount.SECOND, "Алгоритми та структури даних 1. Основи алгоритмізації", LectureType.LECTURE, "Вітковська Ірина Іванівна", "302-18", "https://teams.microsoft.com/l/meetup-join/19%3Ad5119507f5ae4485bfc288df012ab877%40thread.tacv2/1630333406988?context=%7B%22Tid%22%3A%22d595e6a1-b90f-4cc6-b12b-1db7f331d222%22%2C%22Oid%22%3A%22aaa7729f-5f32-4c2a-b228-795e6c3fcb15%22%7D"));
        lectures.add(new Lecture(WeekDay.MONDAY, LectureCount.SECOND, WeekCount.SECOND, "Екологічна безпека та цивільний захист", LectureType.LECTURE, "Праховнік Наталія Артурівна", "302-18", "https://meet.google.com/kii-ftbx-vim"));
        lectures.add(new Lecture(WeekDay.MONDAY, LectureCount.THIRD, WeekCount.SECOND, "Лінійна алгебра та аналітична геометрія", LectureType.LECTURE, "Круглова Наталія Володимирівна", "302-18", "https://us02web.zoom.us/j/84953634271?pwd=TzMzS2lNNUQyNUlHZW1uTStNbjA4QT09"));
        lectures.add(new Lecture(WeekDay.MONDAY, LectureCount.FORTH, WeekCount.SECOND, "Основи здорового способу життя (2-10)", LectureType.LECTURE, "Міщук Діана Миколаївна", "302-18", "https://us05web.zoom.us/j/7931740671?pwd=eGxlZ2FJaXhoQi9EUG9mTVZJTDhQdz09"));

        lectures.add(new Lecture(WeekDay.TUESDAY, LectureCount.SECOND, WeekCount.SECOND, "Іноземна мова 1. Практичний курс іноземної мови І", LectureType.PRACTICE, "Бойко Ірина Віталіївна", "227-18", "https://us04web.zoom.us/j/74293924008?pwd=dDlEVDhVR3U1U1d3L3lkTXVqOXpmdz09"));
        lectures.add(new Lecture(WeekDay.TUESDAY, LectureCount.THIRD, WeekCount.SECOND, "Математичний аналіз 1. Диференціальне числення", LectureType.PRACTICE, "Круглова Наталія Володимирівна", "231-18", "https://us02web.zoom.us/j/86872250596?pwd=L2VDVG85U0psQk5mM0M5QmIvUm1DQT09"));

        lectures.add(new Lecture(WeekDay.WEDNESDAY, LectureCount.THIRD, WeekCount.SECOND, "Лінійна алгебра та аналітична геометрія", LectureType.PRACTICE, "Круглова Наталія Володимирівна", "227-18", "https://t.me/joinchat/XD8d4dK3rqxlNGQy"));

        lectures.add(new Lecture(WeekDay.THURSDAY, LectureCount.FIRST, WeekCount.SECOND, "Основи програмування 1. Базові конструкції", LectureType.LABORATORY, "Камінська Поліна Анатоліївна", "422-18", null));
        lectures.add(new Lecture(WeekDay.THURSDAY, LectureCount.SECOND, WeekCount.SECOND, "Українська мова за професійним спрямуванням", LectureType.PRACTICE, "Сидоренко Лілія Миколаївна", "231-18", "https://teams.microsoft.com/l/meetup-join/19%3Ad5119507f5ae4485bfc288df012ab877%40thread.tacv2/1630333406988?context=%7B%22Tid%22%3A%22d595e6a1-b90f-4cc6-b12b-1db7f331d222%22%2C%22Oid%22%3A%22aaa7729f-5f32-4c2a-b228-795e6c3fcb15%22%7D"));
        lectures.add(new Lecture(WeekDay.THURSDAY, LectureCount.THIRD, WeekCount.SECOND, "Комп'ютерна дискретна математика", LectureType.PRACTICE, "Ліхоузова Тетяна Анатоліївна", "431-18", "https://t.me/joinchat/SWwPzWYpJ9dJsvCE"));

        lectures.add(new Lecture(WeekDay.FRIDAY, LectureCount.FIRST, WeekCount.SECOND, "Математичний аналіз 1. Диференціальне числення", LectureType.LECTURE, "Боднарчук Семен Володимирович", "303-18", "https://www.youtube.com/channel/UC2xqkl7Ic5BV5jGCVC8OznQ"));
        lectures.add(new Lecture(WeekDay.FRIDAY, LectureCount.SECOND, WeekCount.SECOND, "Основи програмування 1. Базові конструкції", LectureType.LECTURE, "Муха Ірина Павлівна", "303-18", "https://zoom.us/j/99775394017?pwd=aGhXMDlYZUd4K0h5aDVBbGdyZmY3QT09"));
        lectures.add(new Lecture(WeekDay.FRIDAY, LectureCount.THIRD, WeekCount.SECOND, "Комп'ютерна дискретна математика", LectureType.LECTURE, "Ліхоузова Тетяна Анатоліївна", "303-18", "https://t.me/joinchat/SWwPzWYpJ9dJsvCE"));

        lectures = Collections.unmodifiableList(lectures);
    }

    public static void setUsers() {
        users = new ArrayList<>();

        users.add(new User("Мадины", 875442644L, "adzhigeldieva", "11.07.2004"));
        users.add(new User("Саши Жабы", 419822524L, null, "25.03.2004"));
        users.add(new User("Прокопенко Алексея", 1074626451L, null, "20.10.2003"));
        users.add(new User("Басана Антона", null, "MX1010A", "09.04.2004"));
        users.add(new User("качка Максима", 728198715L, null, "17.05.2004"));
        users.add(new User("Максима Крестика", 505457346L, "saxxxarius", "28.05.2004"));
        users.add(new User("Кати", null, null, "28.05.2004"));
        users.add(new User("Дианы", 624403801L, null, "07.02.2004"));
        users.add(new User("Владека", null, "allovasneslishno", "07.05.2003"));
        users.add(new User("Андрея", 462070828L, "andrew_kachmar", "17.06.2004"));
        users.add(new User("Кости", 397694208L, "VodilaFireFox", "18.05.2004"));
        users.add(new User("Анжелы", 666150454L, "krishapoehala", "02.04.2004"));
        users.add(new User("Тимура", 1893274358L, "tym0704", "07.07.2004"));
        users.add(new User("Юли", 791497946L, "jkull", "13.06.2004"));
        users.add(new User("Легезы Алексея", 818757464L, null, "12.02.2004"));
        users.add(new User("Лопоши Максима", 799710883L, null, "03.10.2004"));
        users.add(new User("Влада (он же дотер)", 523580673L, "zxcvenorezqwe", "26.05.2004"));
        users.add(new User("Ильи", 544390218L, "illia_ms", "03.11.2003"));
        // users.add(new User("Нездолій Владислав", 621926590L, null, "07.07.2004"));
        users.add(new User("Паши", null, "smartfool", "13.07.2004"));
        users.add(new User("Инги", 1767368044L, "shipperlion", "06.09.2004"));
        users.add(new User("Леры", 839392609L, null, "10.04.2004"));
        users.add(new User("Дани", 1989997739L, "honey_ittsya", "18.03.2003"));
        users.add(new User("Артура", 788249877L, "turrik29", "29.09.2003"));
        users.add(new User("Юры", 581128827L, "fakeeq", "13.08.2004"));
        users.add(new User("Ромы", 680225687L, "Galvinor", "26.06.2004"));
        users.add(new User("Филиппа", null, "dumpling_from_ar", "26.11.2003"));
        users.add(new User("Саши", 564720531L, "ISashaKhI", "22.06.2004"));
        users.add(new User("Артема", null, "Cobalt4555", "24.04.2004"));
        users.add(new User("Миши", 885083447L, "miiixerrr", "20.12.2004"));
        users.add(new User("Щербацкого Антона", 538402282L, "Ent0niyY", "19.01.2004"));

        // users.add(new User("Тимура", 1893274358L, "tym0704", "07.07.2004"));
        // users.add(new User("Тимура", 1893274358L, "tym0704", "07.07.2004"));
        // users.add(new User("Тимура", 1893274358L, "tym0704", "07.07.2004"));
        // users.add(new User("Тимура", 1893274358L, "tym0704", "07.07.2004"));

        users = Collections.unmodifiableList(users);
    }

    // parsing

    @Override
    public void onUpdateReceived(Update update) {
        System.out.println(update);

        if (update.hasMessage()) {
            parseMessage(update.getMessage());
        }
    }

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
            // case "/saturday", "/saturday@ip_14_bot" -> sendWeekDaySchedule(WeekDay.MONDAY, chatId);
            case "/minutes_left", "/minutes_left@ip_14_bot" -> sendMinutesLeft(chatId);
            // case "/mom", "/mom@ip_14_bot" -> mentionMoms(chatId);
        }
    }

    private void parseText(Message message) {
        String text = message.getText();
        Long chatId = message.getChatId();

        if (text.contains("1000-7")) {
            deadInsideCounter(chatId);
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

            if (!getLectures(day, count).isEmpty()) {
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

            if (!getLectures(day, count).isEmpty()) {
                sendSchedule(day, count, chatId);
                return;
            }
        }
    }

    private void sendSchedule(WeekDay day, WeekCount count, Long chatId) {
        List<Lecture> lectureList = getLectures(day, count);

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

    private List<Lecture> getLectures(WeekDay weekDay, WeekCount weekCount) {
        return lectures.stream()
                .filter(lecture -> lecture.getWeekDay() == weekDay && lecture.getWeekCount() == weekCount)
                .sorted(Comparator.comparing(lecture -> lecture.getLectureCount().getCount()))
                .toList();
    }

    private List<Lecture> getTodayLectures() {
        return getLectures(WeekDay.getCurrentWeekDay(), WeekCount.getCurrentWeekCount());
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

        for (User user : users) {
            if (date.equals(user.getBirthdayDate())) {
                String msg = "Сегодня у " + user.getNameWithLink() + " День рождения! " +
                        "Ей (ему) исполняется " + user.getAge(year) + " годиков!";

                sender.sendString(CHAT_ID, msg);
                sender.sendString(CHAT_ID, user.getBirthdayCommand());
            }
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
