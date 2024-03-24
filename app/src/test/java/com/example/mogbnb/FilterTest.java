package com.example.mogbnb;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

public class FilterTest {
    Filter sampleF;
    Filter nullFilter;
    Filter mixedFilter;

    final Date checkInDate = new Date();
    final Date checkOutDate = new Date();
    @Before
    public void setUp() throws Exception {
        sampleF = new Filter("Tomato Town", checkInDate, checkOutDate, 3, 32.5, 4.5);
        nullFilter = new Filter(null, null, null, -1, -1, -1);
        mixedFilter = new Filter("Athens", null, null, 3, 56.6, -1);
    }

    @Test
    public void getArea() {
        Assert.assertEquals("Tomato Town", sampleF.getArea());
        Assert.assertNull(nullFilter.getArea());
        Assert.assertEquals("Athens", mixedFilter.getArea());
    }

    @Test
    public void getCheckIn() {
        Assert.assertEquals(checkInDate, sampleF.getCheckIn());
        Assert.assertNull(nullFilter.getCheckIn());
        Assert.assertNull(mixedFilter.getCheckIn());
    }

    @Test
    public void getCheckOut() {
        Assert.assertEquals(checkOutDate, sampleF.getCheckOut());
        Assert.assertNull(nullFilter.getCheckOut());
        Assert.assertNull(mixedFilter.getCheckOut());
    }

    @Test
    public void getNoOfPersons() {
        Assert.assertEquals(3, sampleF.getNoOfPersons());
        Assert.assertEquals(-1, nullFilter.getNoOfPersons());
        Assert.assertEquals(3, mixedFilter.getNoOfPersons());
    }

    @Test
    public void getPrice() {
        Assert.assertEquals(32.5, sampleF.getPrice(), 0.001);
        Assert.assertEquals(-1, nullFilter.getPrice(), 0.001);
        Assert.assertEquals(56.6, mixedFilter.getPrice(), 0.001);
    }

    @Test
    public void getStars() {
        Assert.assertEquals(4.5, sampleF.getStars(), 0.001);
        Assert.assertEquals(-1, nullFilter.getStars(), 0.001);
        Assert.assertEquals(-1, mixedFilter.getStars(), 0.001);
    }

    @Test
    public void testToString() {
        String printSampleF = "New Filter\n------------------\nArea: Tomato Town\nCheck In: " + checkInDate
                + "\nCheck Out: " + checkOutDate + "\nPeople: 3\nPrice: 32.5\nRating: 4.5";
        String printNullFilter = "New Filter\n------------------\nArea: No Filter\nCheck In: No Filter\nCheck Out: No Filter" +
                "\nPeople: No Filter\nPrice: No Filter\nRating: No Filter";
        String printMixedFilter = "New Filter\n------------------\nArea: Athens\nCheck In: No Filter\nCheck Out: No Filter" +
                "\nPeople: 3\nPrice: 56.6\nRating: No Filter";

        Assert.assertEquals(printSampleF, sampleF.toString());
        Assert.assertEquals(printNullFilter, nullFilter.toString());
        Assert.assertEquals(printMixedFilter, mixedFilter.toString());
    }
}