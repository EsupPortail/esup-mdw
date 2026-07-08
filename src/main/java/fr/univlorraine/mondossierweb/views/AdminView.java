/**
 *
 *  ESUP-Portail MONDOSSIERWEB - Copyright (c) 2016 ESUP-Portail consortium
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package fr.univlorraine.mondossierweb.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.WebBrowser;
import com.vaadin.flow.theme.lumo.LumoUtility;
import fr.univlorraine.mondossierweb.controllers.ConfigController;
import fr.univlorraine.mondossierweb.controllers.LockController;
import fr.univlorraine.mondossierweb.controllers.UiController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.entities.mdw.PreferencesApplication;
import fr.univlorraine.mondossierweb.entities.mdw.PreferencesApplicationCategorie;
import fr.univlorraine.mondossierweb.entities.mdw.UtilisateurSwap;
import fr.univlorraine.mondossierweb.security.MdwUserDetailsService;
import fr.univlorraine.mondossierweb.security.SecurityUtil;
import fr.univlorraine.mondossierweb.utils.PropertyUtils;
import fr.univlorraine.mondossierweb.views.windows.ConfirmWindow;
import fr.univlorraine.mondossierweb.views.windows.InputWindow;
import fr.univlorraine.mondossierweb.views.windows.PreferencesApplicationWindow;
import fr.univlorraine.mondossierweb.views.windows.SwapUtilisateurWindow;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.core.env.Environment;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;

/**
 * Page admin
 */
@Route(value = AdminView.NAME, layout = MainLayout.class)
@Secured(MdwUserDetailsService.CONSULT_ADMINVIEW_AUTORISE)
public class AdminView extends VerticalLayout implements HasDynamicTitle {

	public static final String NAME = "adminView";
	public static final String ROUTE = NAME;

	/* Injections */
	@Resource
	private transient Environment environment;
	@Resource
	private transient UserController userController;
	@Resource
	private transient ConfigController configController;
	@Resource
	private transient UiController uiController;
	@Resource
	private transient LockController lockController;
	@Resource
	private transient ObjectFactory<SwapUtilisateurWindow> swapUtilisateurWindowFactory;
	@Resource
	private transient ObjectFactory<PreferencesApplicationWindow> preferencesApplicationWindowFactory;

	/* Composants */
	private TabSheet tabSheetGlobal;
	private Grid<UtilisateurSwap> confSwapGrid;
	private Grid<UI> uisGrid;
	private Span uisTitle;

	/**
	 * Initialise la vue
	 */
	@PostConstruct
	public void init() {
		//On vérifie le droit d'accéder à la vue
		if (userController.userCanAccessAdminView()) {
			removeAll();
			/* Style */
			setSizeFull();
			setPadding(true);
			setSpacing(true);

			/* En-tete */
			add(buildTopLayout());

			tabSheetGlobal = new TabSheet();
			tabSheetGlobal.setSizeFull();

			/* Récupération des Categories de configuration */
			List<PreferencesApplicationCategorie> categories = configController.getCategoriesOrderByOrdre();
			for (PreferencesApplicationCategorie categorie : categories) {
				tabSheetGlobal.add(new Tab(VaadinIcon.COGS.create(), new Span(categorie.getCatDesc())),
					buildParametresApplicatifsLayout(categorie));
			}

			//ajout de l'onglet 'swap'
			tabSheetGlobal.add(new Tab(VaadinIcon.GROUP.create(), new Span(getTranslation("adminView.tab.swap"))),
				buildSwapLayout());

			//ajout de l'onglet 'sessions/UIs actives'
			tabSheetGlobal.add(new Tab(VaadinIcon.DESKTOP.create(), new Span(getTranslation("adminView.tab.sessions"))),
				buildSessionsLayout());

			add(tabSheetGlobal);
			setFlexGrow(1, tabSheetGlobal);
		}
	}

	@Override
	public String getPageTitle() {
		return getTranslation("adminView.title");
	}

	/**
	 * @return l'en-tête de la vue (titre de l'application, bouton home, version)
	 */
	private HorizontalLayout buildTopLayout() {
		HorizontalLayout topLayout = new HorizontalLayout();
		topLayout.setWidthFull();
		topLayout.setSpacing(true);
		topLayout.setAlignItems(FlexComponent.Alignment.CENTER);

		Span apptitle = new Span(environment.getRequiredProperty("app.name"));
		apptitle.addClassNames(LumoUtility.FontSize.XXLARGE, LumoUtility.FontWeight.BOLD);

		Button homeButton = new Button(VaadinIcon.HOME.create());
		homeButton.addClassName("admin-home-button");
		homeButton.addClickListener(e -> UI.getCurrent().getPage().setLocation(PropertyUtils.getAppUrl()));

		Div titleLayout = new Div();
		titleLayout.setWidthFull();
		titleLayout.addClassName("admin-title-layout");
		titleLayout.add(apptitle, homeButton);

		Span versionLabel = new Span("v" + environment.getRequiredProperty("app.version"));
		versionLabel.addClassName(LumoUtility.FontSize.XSMALL);

		VerticalLayout appTitleLayout = new VerticalLayout(titleLayout, versionLabel);
		appTitleLayout.setPadding(false);
		appTitleLayout.setSpacing(false);
		topLayout.add(appTitleLayout);
		topLayout.setFlexGrow(1, appTitleLayout);

		return topLayout;
	}

	/* Onglet des préférences applicatives d'une catégorie */

	private VerticalLayout buildParametresApplicatifsLayout(PreferencesApplicationCategorie categorie) {
		VerticalLayout layoutConfigApplication = new VerticalLayout();
		layoutConfigApplication.setSizeFull();
		layoutConfigApplication.setSpacing(true);

		/* Grid des conf */
		Grid<PreferencesApplication> confAppGrid = new Grid<>();
		confAppGrid.setSizeFull();
		Grid.Column<PreferencesApplication> colPrefId = confAppGrid.addColumn(PreferencesApplication::getPrefId)
			.setHeader(getTranslation("adminView.confAppTable.prefId")).setSortable(true).setFlexGrow(2);
		confAppGrid.addColumn(PreferencesApplication::getPrefDesc)
			.setHeader(getTranslation("adminView.confAppTable.prefDesc")).setSortable(true).setFlexGrow(3);
		confAppGrid.addColumn(PreferencesApplication::getValeur)
			.setHeader(getTranslation("adminView.confAppTable.valeur")).setSortable(true).setFlexGrow(2);
		confAppGrid.addComponentColumn(prefApp -> {
			Button btnEdit = new Button(getTranslation("adminView.btnEdit"), VaadinIcon.PENCIL.create());
			btnEdit.addThemeVariants(ButtonVariant.LUMO_SMALL);
			btnEdit.addClassName("admin-button");
			btnEdit.addClickListener(e -> editPreferenceApplication(prefApp, confAppGrid, categorie));
			return btnEdit;
		}).setFlexGrow(0).setAutoWidth(true);
		confAppGrid.addItemDoubleClickListener(e -> editPreferenceApplication(e.getItem(), confAppGrid, categorie));
		refreshConfAppGrid(confAppGrid, categorie);
		confAppGrid.sort(GridSortOrder.asc(colPrefId).build());

		layoutConfigApplication.add(confAppGrid);
		layoutConfigApplication.setFlexGrow(1, confAppGrid);
		return layoutConfigApplication;
	}

	private void refreshConfAppGrid(Grid<PreferencesApplication> confAppGrid, PreferencesApplicationCategorie categorie) {
		confAppGrid.setItems(configController.getAppParametersForCatId(categorie.getCatId()));
	}

	private void editPreferenceApplication(PreferencesApplication prefApp, Grid<PreferencesApplication> confAppGrid, PreferencesApplicationCategorie categorie) {
		PreferencesApplicationWindow paw = preferencesApplicationWindowFactory.getObject();
		paw.init(prefApp);
		paw.addOpenedChangeListener(f -> {
			if (!f.isOpened()) {
				refreshConfAppGrid(confAppGrid, categorie);
			}
		});
		paw.open();
	}

	/* Onglet swap utilisateur */

	private VerticalLayout buildSwapLayout() {
		VerticalLayout layoutSwapUser = new VerticalLayout();
		layoutSwapUser.setSizeFull();
		layoutSwapUser.setSpacing(true);

		/* Boutons */
		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setWidthFull();
		buttonsLayout.setSpacing(true);
		buttonsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
		layoutSwapUser.add(buttonsLayout);

		Button btnAddSwap = new Button(getTranslation("adminView.btnAdd"), VaadinIcon.PLUS.create());
		btnAddSwap.addClassName("admin-button");
		btnAddSwap.addClickListener(e -> editSwap(new UtilisateurSwap(), true));
		buttonsLayout.add(btnAddSwap);

		// Deconnexion
		Button decoBtn = new Button(getTranslation("adminView.btnDeconnexion"), VaadinIcon.SIGN_OUT.create());
		decoBtn.addClassName("admin-button");
		decoBtn.addClickListener(e -> {
			userController.disconnectUser();
			UI.getCurrent().getPage().setLocation("logout");
		});
		buttonsLayout.add(decoBtn);

		/* Grid des swaps */
		confSwapGrid = new Grid<>();
		confSwapGrid.setSizeFull();
		Grid.Column<UtilisateurSwap> colLoginSource = confSwapGrid.addColumn(UtilisateurSwap::getLoginSource)
			.setHeader(getTranslation("adminView.confSwapTable.loginSource")).setSortable(true).setFlexGrow(2);
		confSwapGrid.addColumn(UtilisateurSwap::getLoginCible)
			.setHeader(getTranslation("adminView.confSwapTable.loginCible")).setSortable(true).setFlexGrow(2);
		confSwapGrid.addColumn(swap -> swap.getDatCre() == null ? ""
				: new SimpleDateFormat("dd/MM/yyyy HH:mm").format(swap.getDatCre()))
			.setHeader(getTranslation("adminView.confSwapTable.datCre")).setFlexGrow(2);
		confSwapGrid.addComponentColumn(swap -> {
			Button btnEditSwap = new Button(getTranslation("adminView.btnEdit"), VaadinIcon.PENCIL.create());
			btnEditSwap.addThemeVariants(ButtonVariant.LUMO_SMALL);
			btnEditSwap.addClassName("admin-button");
			btnEditSwap.addClickListener(e -> editSwap(swap, false));
			return btnEditSwap;
		}).setFlexGrow(0).setAutoWidth(true);
		confSwapGrid.addItemDoubleClickListener(e -> editSwap(e.getItem(), false));
		refreshSwapGrid();
		confSwapGrid.sort(GridSortOrder.asc(colLoginSource).build());

		layoutSwapUser.add(confSwapGrid);
		layoutSwapUser.setFlexGrow(1, confSwapGrid);
		return layoutSwapUser;
	}

	private void refreshSwapGrid() {
		confSwapGrid.setItems(configController.getSwapUtilisateurs());
	}

	private void editSwap(UtilisateurSwap swap, boolean ajout) {
		SwapUtilisateurWindow suw = swapUtilisateurWindowFactory.getObject();
		suw.init(swap, ajout);
		suw.addOpenedChangeListener(f -> {
			if (!f.isOpened()) {
				refreshSwapGrid();
			}
		});
		suw.open();
	}

	/* Onglet sessions/UIs actives */

	private VerticalLayout buildSessionsLayout() {
		VerticalLayout layoutSessions = new VerticalLayout();
		layoutSessions.setSizeFull();
		layoutSessions.setSpacing(true);

		uisTitle = new Span();
		uisTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.FontWeight.BOLD);
		layoutSessions.add(uisTitle);

		Span warning = new Span(getTranslation("admin.uiList.warning"));
		warning.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.ERROR);
		layoutSessions.add(warning);

		/* Boutons */
		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setWidthFull();
		buttonsLayout.setSpacing(true);
		buttonsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
		layoutSessions.add(buttonsLayout);

		Button btnRefresh = new Button(getTranslation("adminView.sessions.btnRefresh"), VaadinIcon.REFRESH.create());
		btnRefresh.addClassName("admin-button");
		btnRefresh.addClickListener(e -> refreshUisGrid());
		buttonsLayout.add(btnRefresh);

		Button btnSendMessage = new Button(getTranslation("admin.sendMessage.title"), VaadinIcon.COMMENT.create());
		btnSendMessage.addClassName("admin-button");
		btnSendMessage.addClickListener(e -> sendMessage());
		buttonsLayout.add(btnSendMessage);

		/* Grid des UIs connectées */
		uisGrid = new Grid<>();
		uisGrid.setSizeFull();
		uisGrid.addColumn(this::getUsername)
			.setHeader(getTranslation("adminView.sessions.colUtilisateur")).setSortable(true).setFlexGrow(2);
		uisGrid.addColumn(this::getSessionId)
			.setHeader(getTranslation("adminView.sessions.colSession")).setFlexGrow(2);
		uisGrid.addColumn(ui -> String.valueOf(ui.getUIId()))
			.setHeader(getTranslation("adminView.sessions.colUi")).setFlexGrow(0).setAutoWidth(true);
		uisGrid.addColumn(this::getBrowserInfo)
			.setHeader(getTranslation("admin.uiList.column.propertyInfo")).setFlexGrow(3);
		uisGrid.addColumn(this::getActiveView)
			.setHeader(getTranslation("adminView.sessions.colVue")).setFlexGrow(2);
		uisGrid.addComponentColumn(this::buildLocksColumn)
			.setHeader(getTranslation("adminView.sessions.colVerrous")).setFlexGrow(2);
		uisGrid.addComponentColumn(this::buildKillButtons)
			.setHeader(getTranslation("adminView.sessions.colActions")).setFlexGrow(0).setAutoWidth(true);
		refreshUisGrid();

		layoutSessions.add(uisGrid);
		layoutSessions.setFlexGrow(1, uisGrid);
		return layoutSessions;
	}

	private void refreshUisGrid() {
		List<UI> uis = uiController.getUis();
		uisTitle.setText(getTranslation("admin.uiList.title", uis.size()));
		uisGrid.setItems(uis);
	}

	private String getUsername(UI ui) {
		return SecurityUtil.getAuthentication(ui).map(Authentication::getName).orElse("");
	}

	private String getSessionId(UI ui) {
		VaadinSession session = ui.getSession();
		if (session != null && session.getSession() != null) {
			return session.getSession().getId();
		}
		return "";
	}

	private String getBrowserInfo(UI ui) {
		VaadinSession session = ui.getSession();
		if (session != null && session.getBrowser() != null) {
			WebBrowser browser = session.getBrowser();
			return getTranslation("admin.uiList.item.session.propertyInfo", browser.getAddress(), browser.getBrowserApplication());
		}
		return "";
	}

	private String getActiveView(UI ui) {
		try {
			return ui.getInternals().getActiveViewLocation().getPath();
		} catch (Exception e) {
			return "";
		}
	}

	private Div buildLocksColumn(UI ui) {
		Div locksLayout = new Div();
		for (Object lock : lockController.getUILocks(ui)) {
			Button btnRemoveLock = new Button(getTranslation("admin.uiList.btnRemoveLock"), VaadinIcon.UNLOCK.create());
			btnRemoveLock.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY_INLINE);
			btnRemoveLock.setTooltipText(getTranslation("admin.uiList.item.lock.propertyInfo", lock));
			btnRemoveLock.addClickListener(e -> confirmRemoveLock(lock));
			locksLayout.add(btnRemoveLock);
		}
		return locksLayout;
	}

	private HorizontalLayout buildKillButtons(UI ui) {
		HorizontalLayout actionsLayout = new HorizontalLayout();
		actionsLayout.setSpacing(true);

		Button btnKillUser = new Button(VaadinIcon.USER_CLOCK.create());
		btnKillUser.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
		btnKillUser.setTooltipText(getTranslation("adminView.sessions.btnKillUser"));
		btnKillUser.addClickListener(e -> confirmKillUser(ui));
		actionsLayout.add(btnKillUser);

		Button btnKillSession = new Button(VaadinIcon.EXIT.create());
		btnKillSession.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
		btnKillSession.setTooltipText(getTranslation("adminView.sessions.btnKillSession"));
		btnKillSession.addClickListener(e -> confirmKillSession(ui));
		actionsLayout.add(btnKillSession);

		Button btnKillUI = new Button(VaadinIcon.CLOSE_CIRCLE.create());
		btnKillUI.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
		btnKillUI.setTooltipText(getTranslation("adminView.sessions.btnKillUI"));
		btnKillUI.addClickListener(e -> confirmKillUI(ui));
		actionsLayout.add(btnKillUI);

		return actionsLayout;
	}

	/**
	 * Confirme la fermeture de toutes les sessions associées à l'utilisateur de l'UI
	 * (l'ancien UiController.confirmKillUser est désormais à la charge de la vue)
	 * @param ui
	 */
	private void confirmKillUser(UI ui) {
		SecurityUtil.getAuthentication(ui).ifPresent(authentication -> {
			if (authentication.getPrincipal() instanceof UserDetails user) {
				ConfirmWindow confirmWindow = new ConfirmWindow(
					getTranslation("admin.uiList.confirmKillUser", user.getUsername()),
					getTranslation("confirmWindow.defaultTitle"));
				confirmWindow.addBtnOuiListener(e -> {
					uiController.killUser(user);
					refreshUisGrid();
				});
				confirmWindow.open();
			}
		});
	}

	/**
	 * Confirme la fermeture d'une session
	 * (l'ancien UiController.confirmKillSession est désormais à la charge de la vue)
	 * @param ui
	 */
	private void confirmKillSession(UI ui) {
		VaadinSession session = ui.getSession();
		if (session != null) {
			ConfirmWindow confirmWindow = new ConfirmWindow(
				getTranslation("admin.uiList.confirmKillSession", getSessionId(ui), getUsername(ui)),
				getTranslation("confirmWindow.defaultTitle"));
			confirmWindow.addBtnOuiListener(e -> {
				uiController.killSession(session);
				refreshUisGrid();
			});
			confirmWindow.open();
		}
	}

	/**
	 * Confirme la fermeture d'une UI
	 * (l'ancien UiController.confirmKillUI est désormais à la charge de la vue)
	 * @param ui
	 */
	private void confirmKillUI(UI ui) {
		ConfirmWindow confirmWindow = new ConfirmWindow(
			getTranslation("admin.uiList.confirmKillUI"),
			getTranslation("confirmWindow.defaultTitle"));
		confirmWindow.addBtnOuiListener(e -> {
			uiController.killUI(ui);
			refreshUisGrid();
		});
		confirmWindow.open();
	}

	/**
	 * Confirme la suppression d'un verrou
	 * (l'ancien UiController.confirmRemoveLock est désormais à la charge de la vue)
	 * @param lock
	 */
	private void confirmRemoveLock(Object lock) {
		ConfirmWindow confirmWindow = new ConfirmWindow(
			getTranslation("admin.uiList.confirmRemoveLock", lock),
			getTranslation("confirmWindow.defaultTitle"));
		confirmWindow.addBtnOuiListener(e -> {
			lockController.removeLock(lock);
			refreshUisGrid();
		});
		confirmWindow.open();
	}

	/**
	 * Permet la saisie et l'envoi d'un message à tous les clients connectés
	 * (l'ancien UiController.sendMessage est désormais à la charge de la vue)
	 */
	private void sendMessage() {
		InputWindow inputWindow = new InputWindow(
			getTranslation("admin.sendMessage.message"),
			getTranslation("admin.sendMessage.title"));
		inputWindow.addBtnOkListener(text -> {
			if (text != null && !text.isEmpty()) {
				uiController.sendNotification(getTranslation("admin.sendMessage.notificationCaption", text));
			}
		});
		inputWindow.open();
	}

}
