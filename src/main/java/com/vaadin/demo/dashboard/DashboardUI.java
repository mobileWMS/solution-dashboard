package com.vaadin.demo.dashboard;

import com.vaadin.demo.dashboard.data.DataProvider;
import com.vaadin.demo.dashboard.data.dummy.DummyDataProvider;
import com.vaadin.demo.dashboard.event.DashboardEventBus;
import com.vaadin.mpr.MprUI;

public class DashboardUI extends MprUI {
    private final DataProvider dataProvider = new DummyDataProvider();
    private final DashboardEventBus dashboardEventbus = new DashboardEventBus();

    public static DataProvider getDataProvider() {
        return ((DashboardUI) getCurrent()).dataProvider;

    }

    public static DashboardEventBus getDashboardEventbus() {
        return ((DashboardUI) getCurrent()).dashboardEventbus;
    }
}
