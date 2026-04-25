package com.hospital.ui.views;

import com.hospital.entity.Conge;
import com.hospital.entity.Employe;
import com.hospital.enums.Statut;
import com.hospital.service.EmployeService;
import com.hospital.service.PlanningService;
import com.hospital.service.ServiceHospitalierService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.theme.lumo.LumoUtility;

@Route("")
@RouteAlias("dashboard")
@PageTitle("Tableau de bord — City Hospital")
@Menu(title = "Tableau de bord", icon = "vaadin:dashboard", order = 0)
public class DashboardView extends VerticalLayout {

    private final EmployeService            employeService;
    private final PlanningService           planningService;
    private final ServiceHospitalierService serviceHospitalierService;

    public DashboardView(EmployeService employeService,
                         PlanningService planningService,
                         ServiceHospitalierService serviceHospitalierService) {
        this.employeService            = employeService;
        this.planningService           = planningService;
        this.serviceHospitalierService = serviceHospitalierService;

        addClassNames(LumoUtility.Padding.LARGE);
        setWidthFull();

        add(
            createPageTitle(),
            createKpiRow(),
            createBottomSection()
        );
    }

    private Component createPageTitle() {
        var title = new H2("Tableau de bord");
        title.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);
        return title;
    }

    private Component createKpiRow() {
        long totalEmployes  = employeService.findAll().size();
        long actifs         = employeService.countActifs();
        long enConge        = employeService.countEnConge();
        long totalServices  = serviceHospitalierService.count();
        long congesAttente  = planningService.countEnAttente();
        long congesApprouves= planningService.countApprouves();

        var row = new FlexLayout(
            kpiCard("Employés",          String.valueOf(totalEmployes),  "vaadin:users",        "var(--lumo-primary-color)"),
            kpiCard("Actifs",            String.valueOf(actifs),         "vaadin:check-circle", "var(--lumo-success-color)"),
            kpiCard("En congé",          String.valueOf(enConge),        "vaadin:calendar-user","var(--lumo-contrast-50pct)"),
            kpiCard("Services",          String.valueOf(totalServices),  "vaadin:hospital",     "var(--lumo-primary-color)"),
            kpiCard("Congés en attente", String.valueOf(congesAttente),  "vaadin:clock",        "var(--lumo-warning-color)"),
            kpiCard("Congés approuvés",  String.valueOf(congesApprouves),"vaadin:thumbs-up",    "var(--lumo-success-color)")
        );
        row.addClassNames(LumoUtility.Gap.MEDIUM, LumoUtility.Margin.Bottom.LARGE);
        row.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        return row;
    }

    private Component kpiCard(String label, String valeur, String iconName, String couleur) {
        var icon = VaadinIcon.valueOf(
            iconName.replace("vaadin:", "").replace("-", "_").toUpperCase()
        ).create();
        icon.setSize("28px");
        icon.getStyle().set("color", couleur);

        var valeurSpan = new Span(valeur);
        valeurSpan.addClassNames(
            LumoUtility.FontSize.XXXLARGE,
            LumoUtility.FontWeight.BOLD
        );
        valeurSpan.getStyle().set("color", couleur);

        var labelSpan = new Span(label);
        labelSpan.addClassNames(
            LumoUtility.FontSize.SMALL,
            LumoUtility.TextColor.SECONDARY
        );

        var content = new VerticalLayout(icon, valeurSpan, labelSpan);
        content.setSpacing(false);
        content.setPadding(false);
        content.addClassNames(LumoUtility.Gap.XSMALL);

        var card = new Div(content);
        card.addClassNames(
            LumoUtility.Background.BASE,
            LumoUtility.BorderRadius.LARGE,
            LumoUtility.Padding.LARGE,
            LumoUtility.BoxSizing.BORDER
        );
        card.getStyle()
            .set("box-shadow",   "var(--lumo-box-shadow-xs)")
            .set("min-width",    "160px")
            .set("flex",         "1 1 160px");

        return card;
    }

    private Component createBottomSection() {
        var row = new FlexLayout(
            createCongesEnAttente(),
            createEmployesParService()
        );
        row.addClassNames(LumoUtility.Gap.LARGE);
        row.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        row.setWidthFull();
        return row;
    }


    private Component createCongesEnAttente() {
        var titre = new H3("Congés en attente");
        titre.addClassNames(LumoUtility.Margin.Bottom.SMALL);

        var grid = new Grid<Conge>(Conge.class, false);
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        grid.setHeight("280px");

        grid.addColumn(c -> c.getEmploye() != null ? c.getEmploye().getNomComplet() : "—")
                .setHeader("Employé").setAutoWidth(true);
        grid.addColumn(c -> c.getDateDebut() + " → " + c.getDateFin())
                .setHeader("Période").setAutoWidth(true);
        grid.addColumn(c -> c.getNombreJours() + " j")
                .setHeader("Durée").setAutoWidth(true);

        grid.setItems(planningService.findByStatut(Conge.StatutConge.EN_ATTENTE));

        var panel = new Div(titre, grid);
        panel.addClassNames(
            LumoUtility.Background.BASE,
            LumoUtility.BorderRadius.LARGE,
            LumoUtility.Padding.LARGE
        );
        panel.getStyle()
            .set("box-shadow", "var(--lumo-box-shadow-xs)")
            .set("flex",       "1 1 320px");
        return panel;
    }

    private Component createEmployesParService() {
        var titre = new H3("Effectifs par service");
        titre.addClassNames(LumoUtility.Margin.Bottom.SMALL);

        record ServiceStat(String service, long total, long actifs) {}

        var stats = serviceHospitalierService.findAllWithEmployes().stream()
                .map(s -> {
                    long total  = s.getEmployes().size();
                    long actifsCount = s.getEmployes().stream()
                            .filter(e -> e.getStatut() == Statut.ACTIF)
                            .count();
                    return new ServiceStat(s.getNom(), total, actifsCount);
                })
                .toList();

        var grid = new Grid<ServiceStat>(ServiceStat.class, false);
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        grid.setHeight("280px");

        grid.addColumn(ServiceStat::service).setHeader("Service").setAutoWidth(true);
        grid.addColumn(ServiceStat::total).setHeader("Total").setAutoWidth(true);
        grid.addColumn(ServiceStat::actifs).setHeader("Actifs").setAutoWidth(true);

        grid.addColumn(stat -> {
            if (stat.total() == 0) return "—";
            int pct = (int) (stat.actifs() * 100 / stat.total());
            return pct + "%";
        }).setHeader("Taux actifs").setAutoWidth(true);

        grid.setItems(stats);

        var panel = new Div(titre, grid);
        panel.addClassNames(
            LumoUtility.Background.BASE,
            LumoUtility.BorderRadius.LARGE,
            LumoUtility.Padding.LARGE
        );
        panel.getStyle()
            .set("box-shadow", "var(--lumo-box-shadow-xs)")
            .set("flex",       "1 1 320px");
        return panel;
    }
}