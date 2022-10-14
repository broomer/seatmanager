package org.vmetl.tablemanager.model;

import java.util.Comparator;
import java.util.Objects;

public class CustomerGroup implements Comparable<CustomerGroup> {
    private final int size;
    private final int groupId;

    public CustomerGroup(int size, int groupId) {
        this.size = size;
        this.groupId = groupId;
    }

    public int getSize() {
        return size;
    }

    @Override
    public int compareTo(CustomerGroup o) {
        return Comparator.comparingInt(CustomerGroup::getSize).
                thenComparingInt(CustomerGroup::getGroupId).
                compare(this, o);
    }

    public int getGroupId() {
        return groupId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomerGroup that = (CustomerGroup) o;
        return size == that.size && groupId == that.groupId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(size, groupId);
    }
}
