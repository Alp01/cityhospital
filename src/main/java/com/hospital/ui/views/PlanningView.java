package com.hospital.ui.views;

import com.hospital.entity.Conge;
import com.hospital.entity.Employe;
import com.hospital.service.EmployeService;
import com.hospital.service.PlanningService;
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
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

@Route("planning")
@PageTitle("Planning — City Hospital")
@Menu(title = "Planning & Congés", icon = "vaadin:calendar", order = 3)
public class PlanningView extends VerticalLayout {

    private final Grid<Conge> grid = new Grid<>(Conge.class, false);

    private final ComboBox<Conge.StatutConge> filtreStatut = new ComboBox<>("Filtrer par statut");

    private final ComboBox<Employe>          employe      = new ComboBox<>("Employé");
    private final ComboBox<Conge.TypeConge>  type         = new ComboBox<>("Type de congé");
    private final DatePicker                 dateDebut    = new DatePicker("Date de début");
    private final DatePicker                 dateFin      = new DatePicker("Date de fin");
    private final ComboBox<Conge.StatutConge> statut      = new ComboBox<>("Statut");
    private final TextArea                   motif        = new TextArea("Motif");
    private final TextArea                   commentaireRh = new TextArea("Commentaire RH");

    private final Button btnSauvegarder = new Button("Sauvegarder", VaadinIcon.CHECK.create());
    private final Button btnApprouver   = new Button("Approuver",   VaadinIcon.THUMBS_UP.create());
    private final Button btnRefuser     = new Button("Refuser",     VaadinIcon.THUMBS_DOWN.create());
    private final Button btnSupprimer   = new Button("Supprimer",   VaadinIcon.TRASH.create());
    private final Button btnAnnuler     = new Button("Annuler",     VaadinIcon.CLOSE.create());
    private final Button btnNouveau     = new Button("Nouveau congé", VaadinIcon.PLUS.create());

    private final Binder<Conge> binder = new Binder<>(Conge.class);

    private final PlanningService planningService;
    private final EmployeService  employeService;

    private Conge congeEnCours = new Conge();

    public PlanningView(PlanningService planningService, EmployeService employeService) {
        this.planningService = planningService;
        this.employeService  = employeService;

        add(new H2("Planning & Congés"));

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

        rafraichirGrid();
        fermerFormulaire();
    }

    private HorizontalLayout createToolbar() {
        filtreStatut.setItems(Conge.StatutConge.values());
        filtreStatut.setItemLabelGenerator(this::labelStatut);
        filtreStatut.setClearButtonVisible(true);
        filtreStatut.setPlaceholder("Tous les statuts");
        filtreStatut.addValueChangeListener(e -> rafraichirGrid());

        btnNouveau.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        var toolbar = new HorizontalLayout(filtreStatut, btnNouveau);
        toolbar.setAlignItems(Alignment.BASELINE);
        toolbar.addClassNames(LumoUtility.Padding.SMALL, LumoUtility.Gap.SMALL);
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

        grid.addColumn(c -> c.getEmploye() != null ? c.getEmploye().getNomComplet() : "—")
                .setHeader("Employé")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addColumn(c -> c.getEmploye() != null && c.getEmploye().getService() != null
                        ? c.getEmploye().getService().getNom() : "—")
                .setHeader("Service")
                .setAutoWidth(true);

        grid.addColumn(c -> c.getType() != null ? labelType(c.getType()) : "—")
                .setHeader("Type")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addColumn(c -> c.getDateDebut() != null ? c.getDateDebut().toString() : "—")
                .setHeader("Début")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addColumn(c -> c.getDateFin() != null ? c.getDateFin().toString() : "—")
                .setHeader("Fin")
                .setAutoWidth(true);

        grid.addColumn(c -> c.getNombreJours() + " j")
                .setHeader("Durée")
                .setAutoWidth(true);

        grid.addColumn(new ComponentRenderer<>(c -> {
            var badge = new Span(labelStatut(c.getStatut()));
            badge.getElement().getThemeList().add("badge");
            switch (c.getStatut()) {
                case APPROUVE  -> badge.getElement().getThemeList().add("success");
                case EN_ATTENTE -> badge.getElement().getThemeList().add("contrast");
                case REFUSE    -> badge.getElement().getThemeList().add("error");
                case ANNULE    -> badge.getElement().getThemeList().add("error");
            }
            return badge;
        })).setHeader("Statut").setAutoWidth(true);

        grid.asSingleSelect().addValueChangeListener(evt -> {
            if (evt.getValue() != null) ouvrirFormulaire(evt.getValue());
            else fermerFormulaire();
        });
    }

    private VerticalLayout createFormLayout() {
        employe.setItems(employeService.findAll());
        employe.setItemLabelGenerator(Employe::getNomComplet);
        employe.setRequired(true);

        type.setItems(Conge.TypeConge.values());
        type.setItemLabelGenerator(this::labelType);
        type.setRequired(true);

        statut.setItems(Conge.StatutConge.values());
        statut.setItemLabelGenerator(this::labelStatut);

        dateDebut.setRequired(true);
        dateFin.setRequired(true);

        dateDebut.addValueChangeListener(e -> {
            if (e.getValue() != null) dateFin.setMin(e.getValue());
        });

        motif.setMaxLength(500);
        motif.setHeight("80px");
        commentaireRh.setMaxLength(500);
        commentaireRh.setHeight("80px");
        commentaireRh.setPlaceholder("Commentaire de la RH…");

        var form = new FormLayout(employe, type, dateDebut, dateFin,
                statut, motif, commentaireRh);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0",     1),
                new FormLayout.ResponsiveStep("400px", 2)
        );
        form.setColspan(motif,         2);
        form.setColspan(commentaireRh, 2);

        btnSauvegarder.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnApprouver.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        btnRefuser.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        btnSupprimer.addThemeVariants(ButtonVariant.LUMO_ERROR);
        btnAnnuler.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        var boutons = new HorizontalLayout(btnSauvegarder, btnApprouver, btnRefuser, btnSupprimer, btnAnnuler);
        boutons.addClassNames(LumoUtility.Padding.Top.MEDIUM);

        var layout = new VerticalLayout(form, boutons);
        layout.addClassNames(LumoUtility.Padding.MEDIUM);
        layout.setSizeFull();
        return layout;
    }

    private void configurerBinder() {
        binder.forField(employe)
                .asRequired("L'employé est obligatoire")
                .bind(Conge::getEmploye, Conge::setEmploye);

        binder.forField(type)
                .asRequired("Le type est obligatoire")
                .bind(Conge::getType, Conge::setType);

        binder.forField(dateDebut)
                .asRequired("La date de début est obligatoire")
                .bind(Conge::getDateDebut, Conge::setDateDebut);

        binder.forField(dateFin)
                .asRequired("La date de fin est obligatoire")
                .withValidator(fin -> {
                    var debut = dateDebut.getValue();
                    return debut == null || !fin.isBefore(debut);
                }, "La date de fin doit être après la date de début")
                .bind(Conge::getDateFin, Conge::setDateFin);

        binder.forField(statut)
                .bind(Conge::getStatut, Conge::setStatut);

        binder.forField(motif)
                .bind(Conge::getMotif, Conge::setMotif);

        binder.forField(commentaireRh)
                .bind(Conge::getCommentaireRh, Conge::setCommentaireRh);
    }

    private void configurerBoutons() {
        btnNouveau.addClickListener(e -> ouvrirFormulaire(new Conge()));

        btnAnnuler.addClickListener(e -> {
            fermerFormulaire();
            grid.asSingleSelect().clear();
        });

        btnSauvegarder.addClickListener(e -> sauvegarder());

        btnApprouver.addClickListener(e -> changerStatut(true));
        btnRefuser.addClickListener(e -> changerStatut(false));

        btnSupprimer.addClickListener(e -> {
            var dialog = new ConfirmDialog();
            dialog.setHeader("Supprimer ce congé ?");
            dialog.setText("Cette action est irréversible.");
            dialog.setCancelable(true);
            dialog.setConfirmText("Supprimer");
            dialog.setConfirmButtonTheme("error primary");
            dialog.addConfirmListener(ev -> {
                planningService.supprimer(congeEnCours);
                rafraichirGrid();
                fermerFormulaire();
                grid.asSingleSelect().clear();
                notif("Congé supprimé", false);
            });
            dialog.open();
        });
    }

    private void sauvegarder() {
        if (binder.writeBeanIfValid(congeEnCours)) {
            try {
                planningService.sauvegarder(congeEnCours);
                rafraichirGrid();
                fermerFormulaire();
                grid.asSingleSelect().clear();
                notif("Congé sauvegardé ✓", true);
            } catch (IllegalArgumentException ex) {
                Notification.show(ex.getMessage(), 4000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        }
    }

    private void changerStatut(boolean approuver) {
        if (congeEnCours == null || congeEnCours.getId() == null) return;
        try {
            if (approuver) {
                planningService.approuver(congeEnCours.getId(), commentaireRh.getValue());
                notif("Congé approuvé ✓", true);
            } else {
                planningService.refuser(congeEnCours.getId(), commentaireRh.getValue());
                notif("Congé refusé", false);
            }
            rafraichirGrid();
            fermerFormulaire();
            grid.asSingleSelect().clear();
        } catch (IllegalArgumentException ex) {
            Notification.show(ex.getMessage(), 4000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void ouvrirFormulaire(Conge conge) {
        congeEnCours = conge;
        binder.readBean(conge);
        boolean existant = conge.getId() != null;
        btnSupprimer.setVisible(existant);
        boolean enAttente = existant && conge.getStatut() == Conge.StatutConge.EN_ATTENTE;
        btnApprouver.setVisible(enAttente);
        btnRefuser.setVisible(enAttente);
    }

    private void fermerFormulaire() {
        congeEnCours = new Conge();
        binder.readBean(congeEnCours);
        btnSupprimer.setVisible(false);
        btnApprouver.setVisible(false);
        btnRefuser.setVisible(false);
    }

    private void rafraichirGrid() {
        var statutFiltre = filtreStatut.getValue();
        if (statutFiltre != null) {
            grid.setItems(planningService.findByStatut(statutFiltre));
        } else {
            grid.setItems(planningService.findAll());
        }
    }

    private void notif(String message, boolean success) {
        var n = Notification.show(message, 3000, Notification.Position.BOTTOM_END);
        n.addThemeVariants(success
                ? NotificationVariant.LUMO_SUCCESS
                : NotificationVariant.LUMO_CONTRAST);
    }

    private String labelStatut(Conge.StatutConge s) {
        return switch (s) {
            case EN_ATTENTE -> "En attente";
            case APPROUVE   -> "Approuvé";
            case REFUSE     -> "Refusé";
            case ANNULE     -> "Annulé";
        };
    }

    private String labelType(Conge.TypeConge t) {
        return switch (t) {
            case CONGE_ANNUEL -> "Congé annuel";
            case MALADIE      -> "Maladie";
            case MATERNITE    -> "Maternité";
            case PATERNITE    -> "Paternité";
            case FORMATION    -> "Formation";
            case SANS_SOLDE   -> "Sans solde";
            case AUTRE        -> "Autre";
        };
    }
}