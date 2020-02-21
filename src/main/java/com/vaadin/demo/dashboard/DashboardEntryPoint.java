package com.vaadin.demo.dashboard;

import com.google.common.eventbus.Subscribe;
import com.vaadin.demo.dashboard.domain.User;
import com.vaadin.demo.dashboard.event.DashboardEvent.BrowserResizeEvent;
import com.vaadin.demo.dashboard.event.DashboardEvent.CloseOpenWindowsEvent;
import com.vaadin.demo.dashboard.event.DashboardEvent.UserLoggedOutEvent;
import com.vaadin.demo.dashboard.event.DashboardEvent.UserLoginRequestedEvent;
import com.vaadin.demo.dashboard.event.DashboardEventBus;
import com.vaadin.demo.dashboard.view.LoginView;
import com.vaadin.demo.dashboard.view.MainView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.InitialPageSettings;
import com.vaadin.flow.server.PageConfigurator;
import com.vaadin.mpr.LegacyWrapper;
import com.vaadin.mpr.core.LegacyUI;
import com.vaadin.mpr.core.MprTheme;
import com.vaadin.mpr.core.MprWidgetset;
import com.vaadin.server.Page;
import com.vaadin.server.Page.BrowserWindowResizeEvent;
import com.vaadin.server.Page.BrowserWindowResizeListener;
import com.vaadin.server.Responsive;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import java.util.Locale;

@MprTheme("dashboard")
@MprWidgetset("com.vaadin.demo.dashboard.DashboardWidgetSet")
@PageTitle("QuickTickets Dashboard")
@SuppressWarnings("serial")
@Route("")
@LegacyUI(DashboardUI.class)
public final class DashboardEntryPoint extends Div implements PageConfigurator {
    private CssLayout content = new CssLayout();

    public DashboardEntryPoint(){
        setSizeFull();
        content.setSizeFull();
        UI.getCurrent().setLocale(Locale.US);
        add(new LegacyWrapper(content));

        DashboardEventBus.register(this);
        Responsive.makeResponsive(content);
        content.addStyleName(ValoTheme.UI_WITH_MENU);

        updateContent(false);

        // Some views need to be aware of browser resize events so a
        // BrowserResizeEvent gets fired to the event bus on every occasion.
        Page.getCurrent().addBrowserWindowResizeListener(
                new BrowserWindowResizeListener() {
                    @Override
                    public void browserWindowResized(
                            final BrowserWindowResizeEvent event) {
                        DashboardEventBus.post(new BrowserResizeEvent());
                    }
                });
    }

    /**
     * Updates the correct content for this UI based on the current user status.
     * If the user is logged in with appropriate privileges, main view is shown.
     * Otherwise login view is shown.
     *
     * @param isDebug whether the debug functionality should be displayed
     */
    private void updateContent(boolean isDebug) {
        User user = (User) VaadinSession.getCurrent().getAttribute(
                User.class.getName());
        if (user != null && "admin".equals(user.getRole())) {
            // Authenticated user
            content.removeAllComponents();
            content.addComponent(new MainView(isDebug));
            content.removeStyleName("loginview");
            UI.getCurrent().getNavigator().navigateTo(UI.getCurrent().getNavigator().getState());
        } else {
            content.removeAllComponents();
            content.addComponent(new LoginView());
            content.addStyleName("loginview");
        }
    }

    @Subscribe
    public void userLoginRequested(final UserLoginRequestedEvent event) {
        User user = DashboardUI.getDataProvider().authenticate(event.getUserName(),
                event.getPassword());
        VaadinSession.getCurrent().setAttribute(User.class.getName(), user);
        updateContent(event.isDebug());
    }

    @Subscribe
    public void userLoggedOut(final UserLoggedOutEvent event) {
        VaadinSession.getCurrent().getSession().invalidate();
        com.vaadin.flow.component.UI.getCurrent().getPage().reload();
    }

    @Subscribe
    public void closeOpenWindows(final CloseOpenWindowsEvent event) {
        for (Window window : UI.getCurrent().getWindows()) {
            window.close();
        }
    }

    @Override
    public void configurePage(InitialPageSettings initialPageSettings) {
        initialPageSettings.addMetaTag("viewport",
                "width=device-width, initial-scale=1, maximum-scale=1.0, user-scalable=no");
        initialPageSettings.addMetaTag("apple-mobile-web-app-capable", "yes");
        initialPageSettings.addMetaTag("apple-mobile-web-app-status-bar-style",
                "black-translucent");

        initialPageSettings.addLink("apple-touch-icon",
                "/VAADIN/themes/dashboard/img/app-icon.png");
    }
}
