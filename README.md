# seatmanager

This project contains the solution of the SeatManager coding challange.

Methods description:

void arrives(CustomerGroup group) - method runs with O(1)

void leaves(CustomerGroup group) - method runs with O(n), n - the size of the waiting queue

Table locate(CustomerGroup group) - method runs with O(1)

Total amount of consumed memory equals to all object occupying Tables, and CustomerGroups, including those that are on the waiting queue, plus collections overhead (like map for reverse group-to-table).
