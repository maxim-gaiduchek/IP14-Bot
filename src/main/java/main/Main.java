package main;

import datasource.DatasourceConfig;
import datasource.services.DBService;
import entities.Lecture;
import entities.User;
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
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;

public class Main extends TelegramLongPollingBot {

    private static final Long CHAT_ID = -1001598116577L;
    private static final String BOT_USERNAME = System.getenv("BOT_USERNAME");
    private static final String BOT_TOKEN = System.getenv("BOT_TOKEN");
    private final SimpleSender sender = new SimpleSender(BOT_TOKEN);

    public static final ApplicationContext CONTEXT = new AnnotationConfigApplicationContext(DatasourceConfig.class);
    private final DBService service = (DBService) CONTEXT.getBean("service");

    private static final DateFormat FORMAT_TIME = new SimpleDateFormat("HH:mm");
    private static final DateFormat FORMAT_DAY = new SimpleDateFormat("dd.MM");
    private static final DateFormat FORMAT_DATE = new SimpleDateFormat("dd.MM.yyyy");

    private static final int NOTIFICATION_DELAY = 38; // in minutes

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

            case "/today", "/today@ip_14_bot" -> sendScheduleForToday(chatId);
            case "/lecture", "/lecture@ip_14_bot" -> sendCurrentLectureInfo(chatId);
            case "/next_day", "/next_day@ip_14_bot" -> sendNextDaySchedule(chatId);

            case "/week_1", "/week_1@ip_14_bot" -> sendWeekSchedule(WeekCount.FIRST, chatId, message.isUserMessage());
            case "/week_2", "/week_2@ip_14_bot" -> sendWeekSchedule(WeekCount.SECOND, chatId, message.isUserMessage());

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
        } else if ((text.contains("я люблю тебя") || text.contains("я тебя люблю")) && message.isUserMessage()) {
            sendLove(chatId);
        } else if (text.startsWith("я ")) {
            sendMeToo(chatId, text, message.isUserMessage());
        } else if (message.getChatId().equals(CHAT_ID)) {
            animalAttack();
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
                Random random = new Random();

                sender.sendString(chatId, "а знаешь...");
                Thread.sleep(1000);
                sender.sendString(chatId, "я тоже.........................................");
                Thread.sleep(1000);
                sender.sendString(chatId, "ЛЮБЛЮ ТЕБЯ❤️❤️\uD83D\uDC96\uD83D\uDC97\uD83D\uDC97\uD83D\uDC9E\uD83D\uDC96\uD83D\uDC9E❤️❤️\uD83D\uDC93\uD83D\uDC9E\uD83D\uDC97\uD83D\uDC9A\uD83D\uDC97\uD83D\uDC95\uD83D\uDC9A\uD83D\uDC97\uD83D\uDC95\uD83D\uDC9B\uD83D\uDC9A\uD83D\uDC97\uD83D\uDC9A\uD83E\uDDE1\uD83D\uDC9E\uD83D\uDC99\uD83D\uDC97\uD83D\uDC9E\uD83D\uDC9E\uD83D\uDC97\uD83D\uDC95\uD83D\uDC96\uD83D\uDC9A\uD83D\uDC9A\uD83D\uDC96\uD83D\uDC95\uD83D\uDC97\uD83D\uDC9E\uD83D\uDC99\uD83D\uDC93❣️\uD83D\uDC95\uD83E\uDD0E\uD83D\uDC94\uD83D\uDC9F\uD83D\uDDA4\uD83D\uDC94\uD83D\uDC9A\uD83D\uDC98\uD83D\uDC9F\uD83D\uDC9C\uD83D\uDC97❣️\uD83D\uDC97\uD83D\uDDA4\uD83E\uDD0E\uD83D\uDC9E❣️\uD83D\uDC93\uD83D\uDC8C\uD83D\uDC9F\uD83D\uDC96");
                Thread.sleep(1000);
                for (int i = 0; i < 5; i++) {
                    sender.sendSticker(chatId, stickers[random.nextInt(stickers.length)]);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void sendMeToo(Long chatId, String text, boolean isUserMessage) {
        if (isUserMessage || new Random().nextInt(100) == 0) {
            sender.sendString(chatId, "Я тоже " + text.substring(2));
        }
    }

    private void animalAttack() {
        Random random = new Random();
        int randomInt = random.nextInt(5000 * 5);

        if (randomInt == 0) {
            String[] stickersFileIds = new String[]{
                    "CAACAgIAAxkBAAITOWHJr1qYCzve4_rrZW0z_zPwTJv1AAIcDQAC-u-AS2ytN0FC-tDTIwQ",
                    "CAACAgQAAxkBAAITOmHJr1qjDGkBmlm5T1YrCl_ouSLsAAI5AQACJXRTIgdu4Mrb-QIPIwQ",
                    "CAACAgQAAxkBAAITO2HJr1rFAAH4bYcQ39gBpa-gxGTCvwACLAEAAiV0UyLzppr2IgyE7iME",
                    "CAACAgQAAxkBAAITPGHJr1rO1rpfpTiFkdqlmYzmqRMFAAIyAQACJXRTIuphJfNzKMopIwQ",
                    "CAACAgQAAxkBAAITPWHJr1qJ4rJ3FXZ6A4XFJQRGthRRAAI4AQACJXRTIpvX60mnJc6tIwQ",
                    "CAACAgQAAxkBAAITPmHJr1oQ8fN7UYJMxgYWmcKT79khAAI6AQACJXRTIhqsSe3Dmjn-IwQ",
                    "CAACAgQAAxkBAAITP2HJr1q2mEErJ1jPIOa3HOtNM8xcAAI8AQACJXRTIsrJqHO6birLIwQ",
                    "CAACAgQAAxkBAAITQGHJr1pzFzAqO7IZWRouoRlousXhAAJEAQACJXRTIvFetN4r4E8XIwQ",
                    "CAACAgQAAxkBAAITQWHJr1qcWgcH6vVeHLqid8t3V41IAAJFAQACJXRTIslNGYc9TB5VIwQ",
                    "CAACAgQAAxkBAAITQmHJr1qNaNXiZ0JNZl9-kucT5o3CAAJLAQACJXRTIt_bWs_SDAleIwQ",
                    "CAACAgQAAxkBAAITQ2HJr1pMSva881GLCnkGJyFyzVnBAAJMAQACJXRTIkwLWVU6NivnIwQ",
                    "CAACAgQAAxkBAAITRGHJr1oBdnKGwCBtXScZEggZ9HcvAAI0CAACVh6IUxj3zOyGdNcBIwQ",
                    "CAACAgQAAxkBAAITRWHJr1qtQlRiXE6NL2PTjdLI041BAAIVDAACU5lhUTScbHHrMXCbIwQ",
                    "CAACAgQAAxkBAAITRmHJr1rfhzEVSuOhe7K7U0T7mympAAIxAQACJXRTIr2bvUcqde3vIwQ",
                    "CAACAgQAAxkBAAITR2HJr1qIjpnoowgVPqtMR472DOAbAAJDAQACJXRTIqtR7yjK1kRmIwQ",
                    "CAACAgQAAxkBAAITSGHJr1oG8NlmZ6nzWoP0sZBD4ydpAAJGAQACJXRTIuI4Veb7RIoLIwQ",
                    "CAACAgQAAxkBAAITSWHJr1o3m8KvWvI7J8T_hjuau_ecAAJNAQACJXRTItzV8nQdqX-SIwQ",
                    "CAACAgQAAxkBAAITSmHJr1rl6AqViFlPyLXLHZP-3a5jAAJMAQACJXRTIkwLWVU6NivnIwQ",
                    "CAACAgQAAxkBAAITS2HJr1ouF2QJoCp8UthhOaPX3tmlAAIPCQACjeOBU92eBLFIG-YrIwQ",
                    "CAACAgQAAxkBAAITTGHJr1q2Kjuj5Tj8IMXagWPUTpVyAAIoCAACzMmJU99qv_dH6LAcIwQ"
            };

            sender.sendString(CHAT_ID, "ВНЕЗАПНАЯ АТАКА ЖАБАМИ");
            for (int i = 0; i < 5; i++) {
                sender.sendSticker(CHAT_ID, stickersFileIds[random.nextInt(stickersFileIds.length)]);
            }
        } else if (randomInt == 1) {
            String[] stickersFileIds = new String[]{
                    "CAACAgIAAxkBAAIaVGImC4MROWFhQgl1_pOUB8T546P6AAIHFwACytvZS_PuaI4CZskOIwQ",
                    "CAACAgIAAxkBAAIaVWImC4NXTwM_PAABj5VEy3S4aT4h2wAC_RgAAloJ2EsSVt7qvN_c5SME",
                    "CAACAgIAAxkBAAIaVmImC4MiP9T3eg959izClNmfdW23AAKBGQACeXNJSDejZYkEKOpyIwQ",
                    "CAACAgIAAxkBAAIaV2ImC4PLP3DCXy4jYW3X1ijHjHdpAAIyFAACPqvoS10zAlUwGiEXIwQ",
                    "CAACAgIAAxkBAAIaWGImC4Os6m2EYBzOD60btxH0gYIsAAIRGQACF8voS1AaxF-H6-kiIwQ",
                    "CAACAgIAAxkBAAIaWWImC4NZn-4oj8nvTCcUbovo1z0tAAJ4FwACXl7pS5rgW1AxLutmIwQ",
                    "CAACAgIAAxkBAAIaWmImC4MW_vI_LQbuCkUHOotIpTL7AAIPGAACN5joS79CT57DcATZIwQ",
                    "CAACAgIAAxkBAAIaW2ImC4OVGAMEqK8tzhZ7gYHgKx38AAJGFwACl0XpS873AsVNdM42IwQ",
                    "CAACAgIAAxkBAAIaXGImC4PSqWNuTff0GUdGp-E_ruwsAAIQFQACc5PoS1GR1zeY2qFQIwQ",
                    "CAACAgIAAxkBAAIaXWImC4P0ZgiCk3xzfGjyrTHnynE5AAKLFQACvYzxS0ySmLTCdQV6IwQ",
                    "CAACAgIAAxkBAAIaXmImC4NN6Bu9tYFaVgNTJogwSdevAAL2FwAClhLwS3r7AiwAAc4rlSME",
                    "CAACAgIAAxkBAAIaX2ImC4Mc5AAB2TDC3yEr0A-wIx4ljwACmBcAAtIM6Es6Gdj5wk3wcyME",
                    "CAACAgIAAxkBAAIaYGImC4Pw4cdxDsrpzh1i881v0dq1AAIpFwACWWrwS3X4I2eF1DEdIwQ",
                    "CAACAgIAAxkBAAIaYWImC4PwMyV_QkvTeBn7Bopc05eAAAKBEAACf5wRSGWtXqSN60dKIwQ",
                    "CAACAgIAAxkBAAIaYmImC4PSXDkFt5XT4jDkN1dR_Hr6AAJwEgACjW4ZSFFSQvmCO3VYIwQ",
                    "CAACAgIAAxkBAAIaY2ImC4Md4vCVYNmcF5jEa-f6_PKqAAMUAALuRyBIpM31TzD6D6AjBA",
                    "CAACAgIAAxkBAAIaZGImC4NMi0sFCqdMj5w3uKW1aybZAAJcFQACAnDoSzaW1LugBne_IwQ",
                    "CAACAgIAAxkBAAIaZWImC4N4P9JfibHcYHZIaRiJStV7AAK8FQAC58fwSyjtkzH-ENmqIwQ",
                    "CAACAgIAAxkBAAIaZmImC4NrIO6oAY1R8n041DIQbc0dAAKBGAACbSzpSxtR7pCCvbJZIwQ",
                    "CAACAgIAAxkBAAIae2ImEfJJfOlszuiTz_SqD0bXvMVkAAK8EAACDaXQSuRawUcmFtcUIwQ"
            };

            sender.sendString(CHAT_ID, "ВНЕЗАПНАЯ АТАКА КОТАМИ");
            for (int i = 0; i < 5; i++) {
                sender.sendSticker(CHAT_ID, stickersFileIds[random.nextInt(stickersFileIds.length)]);
            }
        } else if (randomInt == 2) {
            String[] stickersFileIds = new String[]{
                    "CgACAgQAAxkBAAIafGImEilqwRiXuIes9NpjWJXn7XFQAALFAgAC7ue9UiiSjYXQmAaiIwQ",
                    "CgACAgQAAxkBAAIafWImEik5kdmggX4MxEZJ0PaIjQMWAAIFAwACiIe0UksXtvwuPDwyIwQ",
                    "CgACAgQAAxkBAAIafmImEinQgXkimydfX-Yc5FuZf-jiAAIMAwAC1Gm9UuOufTW4ph29IwQ",
                    "CgACAgQAAxkBAAIaf2ImEilP4Le4IRJ3UWHWEvAcgKrSAAIKAwACpaK0UtXSzJSLu9oBIwQ",
                    "CgACAgQAAxkBAAIagGImEikj6HoR7_jZjAzo_3GwLxABAAIiAwAC79W0UlgFjQfE9MblIwQ",
                    "CgACAgQAAxkBAAIagWImEinsaMUUcGMItyucnN-7gT9fAAIPAwACYCy8UhpSN_NsWXaVIwQ",
                    "CgACAgQAAxkBAAIagmImEilroTMT0ErMcwdDjvJJ9AAB8QACCgMAAnEetVLvrT8Jy8nHdiME",
                    "CgACAgQAAxkBAAIag2ImEiloN2M6U52sTF34mz4ezDdyAAIFAwACzJvMUvRY1B_guIQTIwQ",
                    "CgACAgQAAxkBAAIahGImEinUS8bslndOKDgKGeV-d2HHAALnAgACAXe9Ug-fVDTp3MmlIwQ",
                    "CgACAgQAAxkBAAIahWImEinK_Hys25-Ks8h5nLZrIKh6AAIYAwACxAe8UpzsLGmq7lBvIwQ",
                    "CgACAgQAAxkBAAIahmImEikl7UGFtwRk6a3VyigTfcdcAAIMAwAC8lG1UmNCsiOMcCZ9IwQ",
                    "CgACAgQAAxkBAAIah2ImEik8KO6KO-2IBtmK70jkApSwAAIqAwAC-9-0UlIR5dmjliAtIwQ",
                    "CgACAgQAAxkBAAIaiGImEinbEnt-R2BdsMZA1Nrqg-lRAAIXAwACg0S9UrK9yXE1Ufs6IwQ",
                    "CgACAgQAAxkBAAIaiWImEil_0Y1m0I7j9Nq5vUjfbl7cAAJjAwACciXtUmNJYf4v4Bs9IwQ",
                    "CgACAgQAAxkBAAIaimImEim12iYZ6Ws1Xb-XVloq5C6lAALSAgACAfDEUpK6uKlrFcIOIwQ",
                    "CgACAgIAAxkBAAIaj2ImFAzZaG-xPlJQtKWkNuoGqCokAAIgFAACn2Y5SGRTJWG8O7dZIwQ",
                    "CgACAgQAAxkBAAIajGImEinioqTiX8D7PAXEVkvuMr1XAAJsAwACZYq1Uo2aTWA8dj4eIwQ",
                    "CgACAgQAAxkBAAIajWImEin_7nsMt7d_n3EVZ_-WxpV5AALoAgACUUC1UmcTagVjDYavIwQ",
                    "CgACAgQAAxkBAAIajmImEik8J6dhZof-9lSHc1Ml_yk_AAIQAwACO9ntUrJoHhi6aeO3IwQ",
                    "AgACAgIAAxkBAAIakGImFNymhaBgH_xucAAB8XqvZq27IAAC4LoxG3f4MUn2BFyGUcu5xAEAAwIAA3MAAyME"
            };

            sender.sendString(CHAT_ID, "ВНЕЗАПНАЯ АТАКА ХОМЯКАМИ");
            for (int i = 0; i < 5; i++) {
                sender.sendDocument(CHAT_ID, stickersFileIds[random.nextInt(stickersFileIds.length)]);
            }
        } else if (randomInt == 3) {
            String[] stickersFileIds = new String[]{
                    "CgACAgQAAxkBAAIakWImGHLMrNs7aQABHw-6NZNNEUVVtgACjQIAAgVilVINZ99e9nOkpiME",
                    "CgACAgQAAxkBAAIakmImGHI9Ng5Kyk6kWe7Xr4qSkrcNAALhAgAC26B1UxvpOZ9H6SEXIwQ",
                    "CgACAgQAAxkBAAIak2ImGHIOmUjvbM5HWHFP0e1-WODOAAIyAwACefr1U71E3R4Laqo0IwQ",
                    "CgACAgQAAxkBAAIalGImGHKE2gj1kDX12re9A_zISFztAAIOAwAC1Pq9UjLDl_GuypZbIwQ",
                    "CgACAgQAAxkBAAIalWImGHIWn-IwvATuvxlziYl8DWSzAAI2AwACm0i1UuY93GuP1_NJIwQ",
                    "CgACAgQAAxkBAAIalmImGHJIPm7Ddbujkgm_PJbK1WpwAAIRAwACl63UUqM71WmoE8oIIwQ",
                    "CgACAgQAAxkBAAIal2ImGHLi_OPB2nellmUYiGpKYHciAAIcAwACa_G9UrTG3bPQYM9FIwQ",
                    "CgACAgQAAxkBAAIamGImGHLOLMseadWKcweyOHFsV9SYAALkAgACKUa8UmvTgJBofaeZIwQ",
                    "CgACAgQAAxkBAAIamWImGHLquLn1nV4K-ISUa-cWAUOqAAIDAwACHai9Uvu71R4cp87NIwQ",
                    "CgACAgQAAxkBAAIammImGHKpFuahPdumgM-yj--AureoAAIGAwAC4Dm9UjgK6TyHp4ZaIwQ",
                    "CgACAgQAAxkBAAIam2ImGHKGb81MCu6LJ0fJefJFy9gOAAJKAwACQiG1UlkC3me9zKZZIwQ",
                    "CgACAgQAAxkBAAIanGImGHLtZKi9pplEyG7xyNJI6GxHAAL-AgACzD-9UhAMChuOuUtwIwQ",
                    "CgACAgQAAxkBAAIanWImGHJuAAFFh4pzM5emfny3qrewFQACGAMAAqqWtVIkR4N9szEQqyM",
                    "CgACAgQAAxkBAAIanmImGHLd2Igl-yzLHCeyUD-4Nk9bAALtAgACDg-8UhEjtJ2WnMjuIwQ",
                    "CgACAgQAAxkBAAIan2ImGHJMf0vdt1EUj1pIE9cqp4xMAALdAgACGgncUr7Y3MS0sTQ5IwQ",
                    "CgACAgQAAxkBAAIaoGImGHI7cw_TetbbatvffrHhwi-xAAILAwACCGjMUmxDjv00lpjSIwQ",
                    "CgACAgQAAxkBAAIaoWImGHJrwC0CFP7_sF5JkzPc1efkAAJuAwACLPe8UnUnRSAYE2qhIwQ",
                    "CgACAgQAAxkBAAIaomImGHKmqL1oUf25BdNH5WjcdiMpAAJMAwACu8W9Um3xSEa8xG2kIwQ",
                    "CgACAgQAAxkBAAIao2ImGHL-aK4lkeKtF3u2ewNDbCLyAALaAgACiCy9UnWdTahrmjTDIwQ",
                    "CgACAgQAAxkBAAIapGImGHJEtEUWNtFXcX4VOR7Q9NdGAAIqAwACC4q8UvYlBJCDfUAGIwQ",
                    "CgACAgIAAxkBAAIapmImGVu0RyrIXI0dzKkz3r_g4wHJAAKgGAACd_gxSQF1IoIzP1KsIwQ"
            };

            sender.sendString(CHAT_ID, "ВНЕЗАПНАЯ АТАКА ЕЖАМИ");
            for (int i = 0; i < 5; i++) {
                sender.sendDocument(CHAT_ID, stickersFileIds[random.nextInt(stickersFileIds.length)]);
            }
        } else if (randomInt == 4) {
            String[] stickersFileIds = new String[]{
                    "BAACAgIAAxkBAAIarGImGk9I273feSh8EWEHJ9wQy0AAA6kYAAJ3-DFJCsuhsgN_UfgjBA",
                    "BAACAgIAAxkBAAIarWImGlhsC1maAWhgXtWyHKhkk1edAAKqGAACd_gxSRaDHfqLL-CIIwQ",
                    "BAACAgIAAxkBAAIarmImGmttltNBz0Iy26sAARtiS2feMQACqxgAAnf4MUlMt4CZUIoQ9yME",
                    "BAACAgIAAxkBAAIar2ImGnF4SKZZMatKqIre7YJ7MXKvAAKsGAACd_gxSf8iApHaoVtEIwQ"
            };

            sender.sendString(CHAT_ID, "ВНЕЗАПНАЯ АТАКА КАРЛИКАМИ");
            for (int i = 0; i < 2; i++) {
                sender.sendDocument(CHAT_ID, stickersFileIds[random.nextInt(stickersFileIds.length)]);
            }
        }
    }

    private void mentionMoms(Long chatId) {
        if (CHAT_ID.equals(chatId)) {
            sender.sendString(chatId, "@ostrich\\_alexey @Pavelperov @andrey\\_rand");
        }
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
                    /*case "07:00" -> {
                        sendScheduleForToday(CHAT_ID);
                        delay(60000, start);
                    }*/
                    case "08:00" -> {
                        sendBirthday();
                        delay(60000, start);
                    }
                    /*default -> {
                        if (sendOnLectureStartsOrEnds(time)) {
                            delay(60000, start);
                        }
                    }*/
                }

                delay(1000, start);
            }
        }

        private void delay(long delay, long start) {
            try {
                sleep(delay - (System.currentTimeMillis() - start) % delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // lectures

    private void sendScheduleForToday(Long chatId) {
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

    private boolean sendOnLectureStartsOrEnds(String time) {
        List<Lecture> lectureList = getTodayLectures();
        String firstNotification = get38MinutesAgo(time);
        String secondNotification = get38MinutesAgo(firstNotification);

        for (int i = 0; i < lectureList.size(); i++) {
            Lecture lecture = lectureList.get(i);
            LectureCount count = lecture.getLectureCount();

            if (time.equals(count.getStartTime())) {
                String msg = i == 0 ? "Первая пара уже начинается:" : "Пара уже начинается:";

                sendLectureInfo(lecture, msg, CHAT_ID);
                return true;
            } else if (time.equals(count.getEndTime())) {
                if (i == lectureList.size() - 1) {
                    sender.sendString(CHAT_ID, "Ура, пары завершились! Вот пары на следующий день");
                    sendNextDaySchedule(CHAT_ID);
                } else {
                    sendLectureInfo(lectureList.get(i + 1), "Пара завершилась. Следущая пара:", CHAT_ID);
                }
                return true;
            } else if (firstNotification.equals(count.getStartTime()) || secondNotification.equals(count.getStartTime())) {
                sendLectureInfo(lecture, "Пара продолжается:", CHAT_ID);
                return true;
            }
        }

        return false;
    }

    private String get38MinutesAgo(String time) {
        try {
            return FORMAT_TIME.format(new Date(FORMAT_TIME.parse(time).getTime() - NOTIFICATION_DELAY * 60 * 1000));
        } catch (ParseException ignored) {
        }

        return time;
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

    private void sendWeekSchedule(WeekCount count, Long chatId, boolean isPrivateChat) {
        if (isPrivateChat) {
            for (int i = 1; i < 6; i++) { // i < 7 if there are Saturdays
                sendSchedule(WeekDay.getWeekDayByCounter(i), count, chatId);
            }
        } else {
            sender.sendStringWithDisabledWebPagePreview(chatId,
                    "Эту команду можно использовать только [в лс бота!](https://t.me/ip_14_bot)");
        }
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
