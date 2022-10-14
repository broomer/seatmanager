package org.vmetl.tablemanager.model;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.BiConsumer;

public class Table implements Comparable<Table> {

    public static final int TABLE_MAX_CAPACITY = 6;
    public static final int TABLE_MIN_CAPACITY = 2;

    private final int size;
    private final int tableNumber; // the unique id of the table, it is a creators responsibility to keep track of unique table numbers

    private int freeSeats;

    public Table(int size, int tableNumber) {

        if (size < TABLE_MIN_CAPACITY || size > TABLE_MAX_CAPACITY) {
            throw new IllegalStateException("Invalid number of seats");
        }

        this.size = size;
        this.tableNumber = tableNumber;
        this.freeSeats = size;
    }

    public int getFreeSeats() {
        return freeSeats;
    }

    public void placeCustomerGroup(CustomerGroup customerGroup, BiConsumer<CustomerGroup, TableChangedEvent> sitsChanged) {
        int previousFreeSeats = freeSeats;
        freeSeats -= customerGroup.getSize();

        sitsChanged.accept(customerGroup, new TableChangedEvent(this, previousFreeSeats));
    }

    public void removeCustomerGroup(CustomerGroup customerGroup, BiConsumer<CustomerGroup, TableChangedEvent> sitsChanged) {
        int previousFreeSeats = freeSeats;
        freeSeats += customerGroup.getSize();

        sitsChanged.accept(customerGroup, new TableChangedEvent(this, previousFreeSeats));
    }

    public boolean isEmpty() {
        return freeSeats == size;
    }

    public boolean isFull() {
        return freeSeats == 0;
    }

    public int getSize() {
        return size;
    }

    @Override
    public int compareTo(Table o) {
        return Comparator.comparingInt(Table::getSize).
                thenComparingInt(Table::getTableNumber).
                compare(this, o);
    }


    public int getTableNumber() {
        return tableNumber;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Table table = (Table) o;
        return size == table.size && tableNumber == table.tableNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(size, tableNumber);
    }

    public static class TableChangedEvent {
        private final Table table;
        private final int previousFreeSeats;

        public TableChangedEvent(Table table, int previousFreeSeats) {
            this.table = table;
            this.previousFreeSeats = previousFreeSeats;
        }

        public Table getTable() {
            return table;
        }

        public int getPreviousFreeSeats() {
            return previousFreeSeats;
        }
    }

}
