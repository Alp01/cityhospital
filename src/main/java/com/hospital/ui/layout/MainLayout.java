package com.hospital.ui.layout;

import com.hospital.security.SecurityService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.avatar.AvatarVariant;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.server.menu.MenuEntry;
import jakarta.annotation.security.PermitAll;

@Layout
@PermitAll
public class MainLayout extends AppLayout {

    private final SecurityService securityService;

    MainLayout(SecurityService securityService) {
        this.securityService = securityService;
        setPrimarySection(Section.DRAWER);
        addToDrawer(
                createApplicationHeader(),
                createApplicationDrawer(),
                createApplicationFooter()
        );
    }

    private Component createApplicationHeader() {
        var appLogo = new Avatar("CH");
        appLogo.addClassName("app-logo");
        appLogo.addThemeVariants(AvatarVariant.AURA_FILLED, AvatarVariant.XSMALL);

        var appName = new Span("City Hospital");
        appName.addClassName("app-name");

        var header = new HorizontalLayout(appLogo, appName);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setPadding(true);
        return header;
    }

    private Component createApplicationDrawer() {
        var scroller = new Scroller(createSideNav());
        scroller.addThemeVariants(ScrollerVariant.OVERFLOW_INDICATORS);
        return scroller;
    }

    private Component createApplicationFooter() {
        var username = securityService.getUsername();
        var role = securityService.isAdmin() ? "Admin" : "RH";

        var userAvatar = new Avatar(username);
        userAvatar.setWidth("32px");
        userAvatar.setHeight("32px");

        var userInfo = new VerticalLayout();
        userInfo.setSpacing(false);
        userInfo.setPadding(false);

        var usernameSpan = new Span(username);
        usernameSpan.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("font-weight", "600");

        var roleSpan = new Span(role);
        roleSpan.getStyle()
                .set("font-size", "var(--lumo-font-size-xs)")
                .set("color", "var(--lumo-secondary-text-color)");

        userInfo.add(usernameSpan, roleSpan);

        var btnLogout = new Button(VaadinIcon.SIGN_OUT.create());
        btnLogout.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        btnLogout.getElement().setAttribute("title", "Se déconnecter");
        btnLogout.addClickListener(e -> securityService.logout());

        var footer = new HorizontalLayout(userAvatar, userInfo, btnLogout);
        footer.setAlignItems(FlexComponent.Alignment.CENTER);
        footer.setWidthFull();
        footer.setPadding(true);
        footer.addClassName("app-footer");
        footer.expand(userInfo);
        return footer;
    }

    private SideNav createSideNav() {
        var nav = new SideNav();
        nav.setMinWidth(200, Unit.PIXELS);
        MenuConfiguration.getMenuEntries()
                .forEach(entry -> nav.addItem(createSideNavItem(entry)));
        return nav;
    }

    private SideNavItem createSideNavItem(MenuEntry menuEntry) {
        if (menuEntry.icon() != null) {
            Component icon;
            if (menuEntry.icon().contains(".svg")) {
                icon = new SvgIcon(menuEntry.icon());
            } else {
                icon = new Icon(menuEntry.icon());
            }
            return new SideNavItem(menuEntry.title(), menuEntry.path(), icon);
        }
        return new SideNavItem(menuEntry.title(), menuEntry.path());
    }
}