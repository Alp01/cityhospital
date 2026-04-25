package com.hospital.ui.views;

import com.hospital.entity.Employe;
import com.hospital.entity.ServiceHospitalier;
import com.hospital.enums.Poste;
import com.hospital.enums.Statut;
import com.hospital.repository.ServiceHospitalierRepository;
import com.hospital.service.EmployeService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
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
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

@Route("employes")
@Menu(title = "Employés", icon = "vaadin:group", order = 1)
@PageTitle("Employés — City Hospital")
public class EmployeView extends VerticalLayout {

    private final Grid<Employe> grid = new Grid<>(Employe.class, false);

    private final TextField                    nom          = new TextField("Nom");
    private final TextField                    prenom       = new TextField("Prénom");
    private final EmailField                   email        = new EmailField("Email");
    private final TextField                    telephone    = new TextField("Téléphone");
    private final DatePicker                   dateEmbauche = new DatePicker("Date d'embauche");
    private final ComboBox<Poste>              poste        = new ComboBox<>("Poste");
    private final ComboBox<Statut>             statut       = new ComboBox<>("Statut");
    private final ComboBox<ServiceHospitalier> service      = new ComboBox<>("Service");

    private final Button btnSauvegarder = new Button("Sauvegarder", VaadinIcon.CHECK.create());
    private final Button btnSupprimer   = new Button("Supprimer",   VaadinIcon.TRASH.create());
    private final Button btnAnnuler     = new Button("Annuler",     VaadinIcon.CLOSE.create());
    private final Button btnNouvel      = new Button("Nouvel employé", VaadinIcon.PLUS.create());

    private final Binder<Employe> binder = new BeanValidationBinder<>(Employe.class);

    private final EmployeService               employeService;
    private final ServiceHospitalierRepository serviceRepo;

    private Employe employeEnCours = new Employe();


    public EmployeView(EmployeService employeService,
                       ServiceHospitalierRepository serviceRepo) {
        this.employeService = employeService;
        this.serviceRepo    = serviceRepo;

        add(new H2("Employés"));

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        var splitLayout = new SplitLayout(createGridLayout(), createFormLayout());
        splitLayout.setSizeFull();
        splitLayout.setSplitterPosition(65);

        add(createToolbar(), splitLayout);

        configurerGrid();
        configurerBinder();
        configurerBoutons();

        rafraichirGrid("");
        fermerFormulaire();
    }

    private HorizontalLayout createToolbar() {
        var recherche = new TextField();
        recherche.setPlaceholder("Rechercher un employé…");
        recherche.setPrefixComponent(VaadinIcon.SEARCH.create());
        recherche.setClearButtonVisible(true);
        recherche.setWidth("300px");
        recherche.addValueChangeListener(e -> rafraichirGrid(e.getValue()));

        btnNouvel.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        var toolbar = new HorizontalLayout(recherche, btnNouvel);
        toolbar.setWidthFull();
        toolbar.setAlignItems(Alignment.CENTER);
        toolbar.addClassNames(LumoUtility.Padding.SMALL, LumoUtility.Gap.SMALL);
        return toolbar;
    }

    private Div createGridLayout() {
        var wrapper = new Div();
        wrapper.setSizeFull();
        wrapper.add(grid);
        return wrapper;
    }

    private void configurerGrid() {
        grid.setSizeFull();
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);

        grid.addColumn(Employe::getNomComplet)
                .setHeader("Nom complet")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addColumn(e -> e.getPoste() != null ? e.getPoste().name() : "—")
                .setHeader("Poste")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addColumn(e -> e.getService() != null ? e.getService().getNom() : "—")
                .setHeader("Service")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addColumn(Employe::getEmail)
                .setHeader("Email")
                .setAutoWidth(true);

        grid.addColumn(new ComponentRenderer<>(e -> {
            var badge = new Span(e.getStatut().name());
            badge.getElement().getThemeList().add("badge");
            switch (e.getStatut()) {
                case ACTIF   -> badge.getElement().getThemeList().add("success");
                case CONGE   -> badge.getElement().getThemeList().add("contrast");
                case INACTIF -> badge.getElement().getThemeList().add("error");
            }
            return badge;
        })).setHeader("Statut").setAutoWidth(true);

        grid.addColumn(e -> e.getDateEmbauche() != null
                        ? e.getDateEmbauche().toString() : "—")
                .setHeader("Embauché le")
                .setAutoWidth(true);

        grid.asSingleSelect().addValueChangeListener(evt -> {
            if (evt.getValue() != null) ouvrirFormulaire(evt.getValue());
            else fermerFormulaire();
        });
    }

    private VerticalLayout createFormLayout() {
        nom.setRequired(true);
        prenom.setRequired(true);
        email.setRequired(true);

        poste.setItems(Poste.values());
        poste.setItemLabelGenerator(Poste::name);

        statut.setItems(Statut.values());
        statut.setItemLabelGenerator(Statut::name);

        service.setItems(serviceRepo.findAll());
        service.setItemLabelGenerator(ServiceHospitalier::getNom);

        var form = new FormLayout(nom, prenom, email, telephone,
                dateEmbauche, poste, statut, service);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0",     1),
                new FormLayout.ResponsiveStep("400px", 2)
        );

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
                .asRequired("Le nom est obligatoire")
                .bind(Employe::getNom, Employe::setNom);

        binder.forField(prenom)
                .asRequired("Le prénom est obligatoire")
                .bind(Employe::getPrenom, Employe::setPrenom);

        binder.forField(email)
                .asRequired("L'email est obligatoire")
                .bind(Employe::getEmail, Employe::setEmail);

        binder.forField(telephone)
                .bind(Employe::getTelephone, Employe::setTelephone);

        binder.forField(dateEmbauche)
                .bind(Employe::getDateEmbauche, Employe::setDateEmbauche);

        binder.forField(poste)
                .bind(Employe::getPoste, Employe::setPoste);

        binder.forField(statut)
                .bind(Employe::getStatut, Employe::setStatut);

        binder.forField(service)
                .bind(Employe::getService, Employe::setService);
    }

    private void configurerBoutons() {
        btnNouvel.addClickListener(e -> ouvrirFormulaire(new Employe()));

        btnAnnuler.addClickListener(e -> {
            fermerFormulaire();
            grid.asSingleSelect().clear();
        });

        btnSauvegarder.addClickListener(e -> sauvegarder());

        btnSupprimer.addClickListener(e -> confirmerSuppression());
    }

    private void sauvegarder() {
        if (binder.writeBeanIfValid(employeEnCours)) {
            employeService.sauvegarder(employeEnCours);
            rafraichirGrid("");
            fermerFormulaire();
            grid.asSingleSelect().clear();
            Notification.show("Employé sauvegardé ✓", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        }
    }

    private void confirmerSuppression() {
        var dialog = new ConfirmDialog();
        dialog.setHeader("Supprimer " + employeEnCours.getNomComplet() + " ?");
        dialog.setText("Cette action est irréversible.");
        dialog.setCancelable(true);
        dialog.setConfirmText("Supprimer");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(e -> {
            employeService.supprimer(employeEnCours);
            rafraichirGrid("");
            fermerFormulaire();
            grid.asSingleSelect().clear();
            Notification.show("Employé supprimé", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_CONTRAST);
        });
        dialog.open();
    }

    private void ouvrirFormulaire(Employe employe) {
        employeEnCours = employe;
        binder.readBean(employe);
        btnSupprimer.setVisible(employe.getId() != null);
    }

    private void fermerFormulaire() {
        employeEnCours = new Employe();
        binder.readBean(employeEnCours);
    }

    private void rafraichirGrid(String filtre) {
        grid.setItems(employeService.rechercher(filtre));
    }
}