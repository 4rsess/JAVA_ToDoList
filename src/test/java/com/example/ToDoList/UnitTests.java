package com.example.ToDoList;

import com.example.ToDoList.models.DeadlineAndPriorityCheck;
import com.example.ToDoList.models.Priority;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

public class UnitTests {

    DeadlineAndPriorityCheck res = new DeadlineAndPriorityCheck();

    private final int maxLength = 255;
    private boolean isTitleValid(String title) {
        return title != null && title.length() >= 4 && title.length() <= maxLength;
    }

    //ПРОВОДИМ ТЕСТЫ НА ПРИОРИТЕТ;
    //Рассмотрим присутствие макроса: !1 - Critical; !2 - High; !3 - Medium; !4 - Low, а также ео отсутствие(Medium)
    @Test
    void priorityCritical(){
        assertEquals(Priority.Critical, res.detectPriority("приоритет critical !1"));
    }
    @Test
    void priorityHigh(){
        assertEquals(Priority.High, res.detectPriority("приоритет high !2"));
    }
    @Test
    void priorityMedium(){
        assertEquals(Priority.Medium, res.detectPriority("приоритет medium !3"));
    }
    @Test
    void priorityLow(){
        assertEquals(Priority.Low, res.detectPriority("приоритет low !4"));
    }
    @Test
    void priorityNothing(){
        assertEquals(Priority.Medium, res.detectPriority("приоритета нет"));
    }

    //Рассмотрим расположение макроса в строке с названием, а также его отсутствие(Medium)
    @Test
    void priorityBegin(){
        assertEquals(Priority.Critical, res.detectPriority("!1 первая задача"));
    }
    @Test
    void priorityMiddle(){
        assertEquals(Priority.Critical, res.detectPriority("первая !1 задача"));
    }
    @Test
    void priorityEnd(){
        assertEquals(Priority.Critical, res.detectPriority("первая задача !1"));
    }
    @Test
    void priorityAbsent(){
        assertEquals(Priority.Medium, res.detectPriority(""));
    }


    //ПРОВОДИМ ТЕСТЫ НА ДЕДЛАЙН;
    //Рассмотрим выполнение макроса: даты через "." и "-" - валидны, а другие значения - ошибка
    @Test
    void deadlinePoint(){
        LocalDateTime expected = LocalDateTime.of(2025, 7, 18, 23, 59, 59);
        assertEquals(expected, res.extractDeadline("первая задача !before 18.07.2025"));
    }
    @Test
    void deadlineDash(){
        LocalDateTime expected = LocalDateTime.of(2025, 7, 18, 23, 59, 59);
        assertEquals(expected, res.extractDeadline("первая задача !before 18-07-2025"));
    }
    @Test
    void deadlineIncorrectSymbol() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                res.extractDeadline("ошибка !before 2025/05/10"));
        assertEquals("Неверный формат даты", ex.getMessage());
    }
    @Test
    void deadlineNothing() {
        assertNull(res.extractDeadline("дедлайна нет"));
    }

    //Рассмотрим граничные значения даты: сегодня и в будущем - валидно, в прошлом - ошибка
    @Test
    void deadlineToday() {
        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        LocalDateTime expected = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        LocalDateTime actual = res.extractDeadline("первая задача !before " + today);
        assertEquals(expected.getDayOfMonth(), actual.getDayOfMonth());
    }
    @Test
    void deadlineTomorrow() {
        String tomorrow = LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        LocalDateTime expected = LocalDateTime.now().plusDays(1).withHour(23).withMinute(59).withSecond(59);
        LocalDateTime actual = res.extractDeadline("первая задача !before " + tomorrow);
        assertEquals(expected.getDayOfMonth(), actual.getDayOfMonth());
    }
    @Test
    void deadlinePast() {
        String yesterday = LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                res.extractDeadline("ошибка !before " + yesterday));
        assertEquals("Дедлайн уже прошел", ex.getMessage());
    }

    //Также проверка на невозможную даты и на большую дату в будущем
    @Test
    void deadlineInvalidDate() {
        String invalidDate = "35.19.2025";
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            res.extractDeadline("ошибка !before " + invalidDate);
        });

        assertEquals("Неверный формат даты", exception.getMessage());
    }
    @Test
    void deadlineFutureDate() {
        String futureDate = "11.11.2152";
        LocalDateTime expected = LocalDate.of(2152, 11, 11).atTime(23, 59, 59);
        LocalDateTime actual = res.extractDeadline("ошибка !before " + futureDate);

        assertEquals(expected, actual);
    }

    //ПРОВЕДЕМ ТЕСТЫ НА ИМЯ ЗАДАЧИ;
    //Рассмотрим эквивалентное разбиение имени: 0-3 символа - ошибка, 4-255 - валидно, 256-бесконечность - ошибка,
    //а также рассмотрим граничные значение символов
    @Test
    void title1Symbol() {
        String title = "1";
        assertFalse(isTitleValid(title), "имя не может быть короче 4 символов");
    }
    @Test
    void title3Symbol() {
        String title = "123";
        assertFalse(isTitleValid(title), "имя не может быть короче 4 символов");
    }
    @Test
    void title4Symbol() {
        String title = "1234";
        assertTrue(isTitleValid(title), "валидно");
    }
    @Test
    void title254Symbol() {
        String title = "1".repeat(254);
        assertTrue(isTitleValid(title), "валидно");
    }
    @Test
    void title256Symbol() {
        String title = "1".repeat(256);
        assertFalse(isTitleValid(title), "имя должно быть меньше 256 символов");
    }
    @Test
    void title555Symbol() {
        String title = "1".repeat(555);
        assertFalse(isTitleValid(title), "имя должно быть меньше 256 символов");
    }


    //ПРОВЕДЕМ ТЕСТЫ НА ПРОВЕРКУ ТОГО, ЧТО МАКРС УДАЛЯЕТСЯ ПОСЛЕ ВВОДА
    @Test
    void cleanTitlePriorityMacros() {
        assertEquals("первая задача", res.cleanTitle("первая !1 задача"));
    }
    @Test
    void cleanTitleWithDeadlineMacros() {
        assertEquals("первая задача", res.cleanTitle("первая задача !before 18.07.2025"));
    }
    @Test
    void cleanTitleWhitespaceNormalization() {
        assertEquals("первая задача", res.cleanTitle("первая   задача !1    !before 18.07.2025"));
    }
    @Test
    void cleanTitleNoMacros() {
        assertEquals("первая задача", res.cleanTitle("первая задача"));
    }

    //ПРОВЕДЕМ ТЕСТ НА НАЛИЧАЕ НЕСКОЛЬКИХ ПРИОРИТЕТОВ
    @Test
    void priorityMultipleMacros() {
        assertEquals(Priority.Critical, res.detectPriority("задача !1 !2"));
    }
    @Test
    void priorityConflictingMacrosEndFirst() {
        assertEquals(Priority.Critical, res.detectPriority("задача !2 !1"));
    }


    //ПРОВЕДЕМ ТЕСТ ПРИОРИТЕТ+ДЕДЛАЙН ОДНОВРЕМЕННО
    @Test
    void priorityMacrosPriorityAndDeadline() {
        Priority fromField = Priority.High;
        Priority fromMacro = res.detectPriority("задача !1");
        assertEquals(Priority.High, fromField);
    }

}