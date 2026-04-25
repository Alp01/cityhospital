package com.hospital.ui.views;

import com.hospital.entity.ServiceHospitalier;
import com.hospital.service.ServiceHospitalierService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.theme.lumo.LumoUtility;

@Route("services")
@PageTitle("Services — City Hospital")
@Menu(title = "Services", icon = "vaadin:hospital", order = 2)
public class ServiceView extends VerticalLayout {

    private final Grid<ServiceHospitalier> grid = new Grid<>(ServiceHospitalier.class, false);

    private final TextField nom         = new TextField("Nom du service");
    private final TextField chefService = new TextField("Chef de service");
    private final TextArea  description = new TextArea("Description");

    private final Button btnSauvegarder = new Button("Sauvegarder", VaadinIcon.CHECK.create());
    private final Button btnSupprimer   = new Button("Supprimer",   VaadinIcon.TRASH.create());
    private final Button btnAnnuler     = new Button("Annuler",     VaadinIcon.CLOSE.create());
    private final Button btnNouveau     = new Button("Nouveau service", VaadinIcon.PLUS.create());

    private final Binder<ServiceHospitalier> binder = new Binder<>(ServiceHospitalier.class);

    private final ServiceHospitalierService serviceMetier;

    private ServiceHospitalier serviceEnCours = new ServiceHospitalier();

    public ServiceView(ServiceHospitalierService serviceMetier) {
        this.serviceMetier = serviceMetier;

        add(new H2("Services"));

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        var splitLayout = new SplitLayout(createGridLayout(), createFormLayout());
        splitLayout.setSizeFull();
        splitLayout.setSplitterPosition(60);

        add(createToolbar(), splitLayout);

        configurerGrid();
        configurerBinder();
        configurerBoutons();

        rafraichirGrid();
        fermerFormulaire();
    }

    private HorizontalLayout createToolbar() {
        btnNouveau.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        var toolbar = new HorizontalLayout(btnNouveau);
        toolbar.addClassNames(LumoUtility.Padding.SMALL);
        return toolbar;
    }

    private Div createGridLayout() {
        var wrapper = new Div(grid);
        wrapper.setSizeFull();
        return wrapper;
    }

    private void configurerGrid() {
        grid.setSizeFull();
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);

        grid.addColumn(ServiceHospitalier::getNom)
                .setHeader("Service")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addColumn(s -> s.getChefService() != null ? s.getChefService() : "—")
                .setHeader("Chef de service")
                .setAutoWidth(true);

        grid.addColumn(s -> s.getDescription() != null ? s.getDescription() : "—")
                .setHeader("Description")
                .setAutoWidth(true);

        grid.addColumn(new ComponentRenderer<>(s -> {
            int nb = s.getEmployes() != null ? s.getEmployes().size() : 0;
            var badge = new Span(nb + " employé" + (nb > 1 ? "s" : ""));
            badge.getElement().getThemeList().add("badge");
            badge.getElement().getThemeList().add(nb > 0 ? "success" : "contrast");
            return badge;
        })).setHeader("Effectif").setAutoWidth(true);

        grid.asSingleSelect().addValueChangeListener(evt -> {
            if (evt.getValue() != null) ouvrirFormulaire(evt.getValue());
            else fermerFormulaire();
        });
    }

    private VerticalLayout createFormLayout() {
        nom.setRequired(true);
        description.setHeight("100px");
        description.setMaxLength(500);

        var form = new FormLayout(nom, chefService, description);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0",     1),
                new FormLayout.ResponsiveStep("400px", 2)
        );
        form.setColspan(description, 2);

        btnSauvegarder.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnSupprimer.addThemeVariants(ButtonVariant.LUMO_ERROR);
        btnAnnuler.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        var boutons = new HorizontalLayout(btnSauvegarder, btnSupprimer, btnAnnuler);
        boutons.addClassNames(LumoUtility.Padding.Top.MEDIUM);

        var layout = new VerticalLayout(form, boutons);
        layout.addClassNames(LumoUtility.Padding.MEDIUM);
        layout.setSizeFull();
        return layout;
    }

    private void configurerBinder() {
        binder.forField(nom)
                .asRequired("Le nom du service est obligatoire")
                .bind(ServiceHospitalier::getNom, ServiceHospitalier::setNom);

        binder.forField(chefService)
                .bind(ServiceHospitalier::getChefService, ServiceHospitalier::setChefService);

        binder.forField(description)
                .bind(ServiceHospitalier::getDescription, ServiceHospitalier::setDescription);
    }

    private void configurerBoutons() {
        btnNouveau.addClickListener(e -> ouvrirFormulaire(new ServiceHospitalier()));

        btnAnnuler.addClickListener(e -> {
            fermerFormulaire();
            grid.asSingleSelect().clear();
        });

        btnSauvegarder.addClickListener(e -> sauvegarder());

        btnSupprimer.addClickListener(e -> {
            var dialog = new ConfirmDialog();
            dialog.setHeader("Supprimer « " + serviceEnCours.getNom() + " » ?");
            dialog.setText("Les employés rattachés à ce service seront dissociés.");
            dialog.setCancelable(true);
            dialog.setConfirmText("Supprimer");
            dialog.setConfirmButtonTheme("error primary");
            dialog.addConfirmListener(ev -> {
                serviceMetier.supprimer(serviceEnCours);
                rafraichirGrid();
                fermerFormulaire();
                grid.asSingleSelect().clear();
                notif("Service supprimé", false);
            });
            dialog.open();
        });
    }

    private void sauvegarder() {
        if (binder.writeBeanIfValid(serviceEnCours)) {
            serviceMetier.sauvegarder(serviceEnCours);
            rafraichirGrid();
            fermerFormulaire();
            grid.asSingleSelect().clear();
            notif("Service sauvegardé ✓", true);
        }
    }

    private void ouvrirFormulaire(ServiceHospitalier service) {
        serviceEnCours = service;
        binder.readBean(service);
        btnSupprimer.setVisible(service.getId() != null);
    }

    private void fermerFormulaire() {
        serviceEnCours = new ServiceHospitalier();
        binder.readBean(serviceEnCours);
        btnSupprimer.setVisible(false);
    }

    private void rafraichirGrid() {
        grid.setItems(serviceMetier.findAllWithEmployes());
    }

    private void notif(String message, boolean success) {
        Notification.show(message, 3000, Notification.Position.BOTTOM_END)
                .addThemeVariants(success
                        ? NotificationVariant.LUMO_SUCCESS
                        : NotificationVariant.LUMO_CONTRAST);
    }
}