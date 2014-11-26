package fr.univlorraine.mondossierweb.views;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import ru.xpoft.vaadin.VaadinView;

import com.vaadin.annotations.StyleSheet;
import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinSession;
import com.vaadin.server.WebBrowser;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import fr.univlorraine.mondossierweb.controllers.LockController;
import fr.univlorraine.mondossierweb.controllers.UiController;
import fr.univlorraine.mondossierweb.controllers.UserController;

/**
 * Informations sur les clients actifs
 * <br/> Sessions / Verrous
 * @author Matthieu MANGINOT
 * @author Adrien Colson
 */
@Component @Scope("prototype")
@VaadinView(AdminView.NAME)
@PreAuthorize("hasRole('ROLE_' + @environment.getRequiredProperty('role.admin'))")
public class AdminView extends VerticalLayout implements View {
	private static final long serialVersionUID = -5357574566017551872L;

	public static final String NAME = "adminView";

	private static final String PROPERTY_ID = "propertyId";
	private static final String PROPERTY_INFO = "propertyInfo";

	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient UserController userController;
	@Resource
	private transient UiController uiController;
	@Resource
	private transient LockController lockController;

	/* Composants */
	private TextField switchUserTextField = new TextField();
	private Button btnSwitchUser;
	private Label clientTreeTableTitle = new Label();
	private Button btnSendMessage;
	private Button btnKill;
	private Button btnRemoveLock;
	private HierarchicalContainer uisContainer = new HierarchicalContainer();
	private TreeTable uisTreeTable = new TreeTable();

	private Integer nbClients = 0;

	/**
	 * Initialise la vue
	 */
	@PostConstruct
	public void init() {
		/* Style */
		setSizeFull();
		setMargin(true);
		setSpacing(true);

		/* Titre */
		Label title = new Label(applicationContext.getMessage(NAME + ".title", null, getLocale()));
		title.addStyleName(ValoTheme.LABEL_H1);
		addComponent(title);

		/* Changement de rôle */
		Label switchUserTitle = new Label(applicationContext.getMessage("admin.switchUser.title", null, getLocale()));
		switchUserTitle.addStyleName(ValoTheme.LABEL_H2);
		addComponent(switchUserTitle);

		HorizontalLayout switchUserLayout = new HorizontalLayout();
		switchUserLayout.setSpacing(true);
		addComponent(switchUserLayout);

		Label switchUserLabel = new Label(applicationContext.getMessage("admin.switchUser.label", null, getLocale()));
		switchUserLayout.addComponent(switchUserLabel);
		switchUserLayout.setComponentAlignment(switchUserLabel, Alignment.MIDDLE_CENTER);

		switchUserTextField.setMaxLength(10);
		switchUserTextField.setImmediate(true);
		switchUserTextField.addTextChangeListener(e -> {
			switchUserTextField.setValue(e.getText());
			/* Le bouton de changement de rôle est actif si un login est entré. */
			btnSwitchUser.setEnabled(StringUtils.hasText(e.getText()));
		});
		switchUserTextField.addShortcutListener(new ShortcutListener(null, ShortcutAction.KeyCode.ENTER, null) {
			private static final long serialVersionUID = 6654068159607621464L;

			@Override
			public void handleAction(Object sender, Object target) {
				userController.switchToUser(switchUserTextField.getValue());
			}
		});
		switchUserLayout.addComponent(switchUserTextField);

		btnSwitchUser = new Button(applicationContext.getMessage("admin.switchUser.btnSwitchUser", null, getLocale()), FontAwesome.SIGN_IN);
		btnSwitchUser.setEnabled(false);
		btnSwitchUser.addClickListener(e -> userController.switchToUser(switchUserTextField.getValue()));
		switchUserLayout.addComponent(btnSwitchUser);

		/* Titre et boutons liste des clients actifs */
		HorizontalLayout clientTreeTableButtonsLayout = new HorizontalLayout();
		clientTreeTableButtonsLayout.setWidth(100, Unit.PERCENTAGE);
		clientTreeTableButtonsLayout.setSpacing(true);
		addComponent(clientTreeTableButtonsLayout);

		clientTreeTableTitle.addStyleName(ValoTheme.LABEL_H2);
		clientTreeTableButtonsLayout.addComponent(clientTreeTableTitle);
		clientTreeTableButtonsLayout.setComponentAlignment(clientTreeTableTitle, Alignment.MIDDLE_LEFT);

		btnSendMessage = new Button(applicationContext.getMessage("admin.sendMessage.title", null, getLocale()), FontAwesome.PENCIL);
		btnSendMessage.setEnabled(UI.getCurrent().getPushConfiguration().getPushMode().isEnabled());
		btnSendMessage.addClickListener(e -> uiController.sendMessage());
		clientTreeTableButtonsLayout.addComponent(btnSendMessage);
		clientTreeTableButtonsLayout.setComponentAlignment(btnSendMessage, Alignment.MIDDLE_CENTER);

		HorizontalLayout clientTreeTableRightButtonsLayout = new HorizontalLayout();
		clientTreeTableRightButtonsLayout.setSpacing(true);
		clientTreeTableButtonsLayout.addComponent(clientTreeTableRightButtonsLayout);
		clientTreeTableButtonsLayout.setComponentAlignment(clientTreeTableRightButtonsLayout, Alignment.MIDDLE_RIGHT);

		btnRemoveLock = new Button(applicationContext.getMessage("admin.uiList.btnRemoveLock", null, getLocale()), FontAwesome.UNLOCK);
		btnRemoveLock.setEnabled(false);
		btnRemoveLock.addClickListener(e -> uiController.confirmRemoveLock(uisTreeTable.getValue()));
		clientTreeTableRightButtonsLayout.addComponent(btnRemoveLock);

		btnKill = new Button(applicationContext.getMessage("admin.uiList.btnkill", null, getLocale()), FontAwesome.TIMES);
		btnKill.setEnabled(false);
		btnKill.addClickListener(e -> {
			Object obj = uisTreeTable.getValue();
			if (obj instanceof UserDetails) {
				uiController.confirmKillUser((UserDetails) obj);
			} else if (obj instanceof VaadinSession) {
				uiController.confirmKillSession((VaadinSession) obj);
			} else if (obj instanceof UI) {
				uiController.confirmKillUI((UI) obj);
			}
		});
		clientTreeTableRightButtonsLayout.addComponent(btnKill);

		/* TreeTable des clients actifs */
		uisContainer.addContainerProperty(PROPERTY_INFO, String.class, null);
		uisContainer.addContainerProperty(PROPERTY_ID, String.class, null);
		uisContainer.sort(new Object[]{PROPERTY_ID}, new boolean[]{true});
		uisTreeTable.setContainerDataSource(uisContainer);
		uisTreeTable.setVisibleColumns(new Object[] {PROPERTY_ID, PROPERTY_INFO});
		uisTreeTable.setColumnHeader(PROPERTY_ID, applicationContext.getMessage("admin.uiList.column." + PROPERTY_ID, null, getLocale()));
		uisTreeTable.setColumnHeader(PROPERTY_INFO, applicationContext.getMessage("admin.uiList.column." + PROPERTY_INFO, null, getLocale()));
		uisTreeTable.setColumnExpandRatio(PROPERTY_INFO, 1);
		uisTreeTable.setSizeFull();
		uisTreeTable.setImmediate(true);
		uisTreeTable.setSelectable(true);
		uisTreeTable.setCellStyleGenerator((source, itemId, propertyId) -> {
			if (itemId.equals(getUI()) || itemId.equals(getSession()) || itemId.equals(uisTreeTable.getParent(getSession()))) {
				return "highlighted";
			}
			return null;
		});
		uisTreeTable.addValueChangeListener(e -> {
			Object selectedObject = uisTreeTable.getValue();
			boolean selectedObjectIsUserOrSessionOrUI = selectedObject instanceof UserDetails || selectedObject instanceof VaadinSession || selectedObject instanceof UI;

			/* Le bouton qui permet de supprimer une session est actif si une session est sélectionnée */
			btnKill.setEnabled(selectedObjectIsUserOrSessionOrUI);
			/* Le bouton qui permet de supprimer un verrou est actif si un verrou est sélectionné */
			btnRemoveLock.setEnabled(selectedObject != null && !selectedObjectIsUserOrSessionOrUI);
		});
		/* Charge la liste d'UIs */
		uiController.getUis().forEach(ui -> addUI(ui));
		addComponent(uisTreeTable);
		setExpandRatio(uisTreeTable, 1);

		/* Informations sur le comportement en cas de suppression */
		addComponent(new Label(FontAwesome.WARNING.getHtml() + " " + applicationContext.getMessage("admin.uiList.warning", null, getLocale()), ContentMode.HTML));

		/* Affiche le nombre d'UIs connectées*/
		updateClientTreeTableTitle();

		/* Inscrit la vue pour la mise à jour de la liste des UIs */
		uiController.registerAdminView(this);
	}

	/**
	 * @see com.vaadin.ui.AbstractComponent#detach()
	 */
	@Override
	public void detach() {
		super.detach();

		/* Désinscrit la vue pour la mise à jour de la liste des UIs */
		uiController.unregisterAdminView(this);
	}

	/**
	 * @see com.vaadin.navigator.View#enter(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
	 */
	@Override
	public void enter(ViewChangeEvent event) {
	}

	/**
	 * Ajoute une UI dans la liste
	 * @param ui
	 */
	@SuppressWarnings("unchecked")
	private void addUI(UI ui) {
		VaadinSession session = ui.getSession();
		SecurityContext securityContext = (SecurityContext) session.getSession().getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
		UserDetails user = (UserDetails) securityContext.getAuthentication().getPrincipal();
		List<Object> uiLocks = lockController.getUILocks(ui);

		/* User item */
		Item userItem = uisContainer.getItem(user);
		if (userItem == null) {
			userItem = uisContainer.addItem(user);
			userItem.getItemProperty(PROPERTY_ID).setValue(applicationContext.getMessage("admin.uiList.item.user." + PROPERTY_ID, new Object[] {user.getUsername()}, getLocale()));
			userItem.getItemProperty(PROPERTY_INFO).setValue(applicationContext.getMessage("admin.uiList.item.user." + PROPERTY_INFO, new Object[] {user.getAuthorities()}, getLocale()));
			uisTreeTable.setCollapsed(user, false);
		}

		/* Session item */
		Item sessionItem = uisContainer.getItem(session);
		if (sessionItem == null) {
			sessionItem = uisContainer.addItem(session);
			uisContainer.setParent(session, user);
			sessionItem.getItemProperty(PROPERTY_ID).setValue(applicationContext.getMessage("admin.uiList.item.session." + PROPERTY_ID, new Object[] {session.getSession().getId()}, getLocale()));
			WebBrowser browser = ui.getPage().getWebBrowser();
			String ipAddress = browser.getAddress();
			String browserInfo = browser.getBrowserApplication() + " v" + browser.getBrowserMajorVersion() + "." + browser.getBrowserMinorVersion();
			sessionItem.getItemProperty(PROPERTY_INFO).setValue(applicationContext.getMessage("admin.uiList.item.session." + PROPERTY_INFO, new Object[] {ipAddress, browserInfo}, getLocale()));
			uisTreeTable.setCollapsed(session, false);
		}

		/* UI item */
		nbClients++;
		Item uiItem = uisContainer.addItem(ui);
		uisContainer.setParent(ui, session);
		uiItem.getItemProperty(PROPERTY_ID).setValue(applicationContext.getMessage("admin.uiList.item.ui." + PROPERTY_ID, new Object[] {ui.getUIId()}, getLocale()));
		uiItem.getItemProperty(PROPERTY_INFO).setValue(applicationContext.getMessage("admin.uiList.item.ui." + PROPERTY_INFO, new Object[] {uiLocks.size(), ui.getNavigator().getState()}, getLocale()));
		uisContainer.setChildrenAllowed(ui, !uiLocks.isEmpty());

		/* Lock items */
		uiLocks.forEach(lock -> {
			Item lockItem = uisContainer.getItem(lock);
			if (lockItem == null) {
				lockItem = uisContainer.addItem(lock);
				uisContainer.setChildrenAllowed(lock, false);
				lockItem.getItemProperty(PROPERTY_ID).setValue(applicationContext.getMessage("admin.uiList.item.lock." + PROPERTY_ID, new Object[] {lock.getClass().getName()}, getLocale()));
			}
			lockItem.getItemProperty(PROPERTY_INFO).setValue(applicationContext.getMessage("admin.uiList.item.lock." + PROPERTY_INFO, new Object[] {lock.toString()}, getLocale()));
			uisContainer.setParent(lock, ui);
		});
	}

	/**
	 * Met à jour une UI de la liste
	 * @param ui
	 */
	@SuppressWarnings("unchecked")
	private void updateUI(UI ui) {
		List<Object> uiLocks = lockController.getUILocks(ui);

		/* UI item */
		Item uiItem = uisContainer.getItem(ui);
		uiItem.getItemProperty(PROPERTY_ID).setValue(applicationContext.getMessage("admin.uiList.item.ui." + PROPERTY_ID, new Object[] {ui.getUIId()}, getLocale()));
		uiItem.getItemProperty(PROPERTY_INFO).setValue(applicationContext.getMessage("admin.uiList.item.ui." + PROPERTY_INFO, new Object[] {uiLocks.size(), ui.getNavigator().getState()}, getLocale()));
		uisContainer.setChildrenAllowed(ui, !uiLocks.isEmpty());

		/* Supprime les anciens locks */
		if (uisContainer.hasChildren(ui)) {
			uisContainer.getChildren(ui)
				.stream().filter(lock -> !uiLocks.contains(lock))
				.forEach(lock -> uisContainer.removeItem(lock));
		}
		/* Ajoute les nouveaux locks */
		uiLocks.forEach(lock -> {
			Item lockItem = uisContainer.getItem(lock);
			if (lockItem == null) {
				lockItem = uisContainer.addItem(lock);
				uisContainer.setChildrenAllowed(lock, false);
				lockItem.getItemProperty(PROPERTY_ID).setValue(applicationContext.getMessage("admin.uiList.item.lock." + PROPERTY_ID, new Object[] {lock.getClass().getName()}, getLocale()));
			}
			lockItem.getItemProperty(PROPERTY_INFO).setValue(applicationContext.getMessage("admin.uiList.item.lock." + PROPERTY_INFO, new Object[] {lock.toString()}, getLocale()));
			uisContainer.setParent(lock, ui);
		});
	}

	/**
	 * Supprime une UI de la liste
	 * @param ui
	 */
	private void removeUI(UI ui) {
		nbClients--;
		VaadinSession session = (VaadinSession) uisContainer.getParent(ui);
		uisContainer.removeItemRecursively(ui);
		if (!uisContainer.hasChildren(session)) {
			UserDetails user = (UserDetails) uisContainer.getParent(session);
			uisContainer.removeItem(session);
			if (!uisContainer.hasChildren(user)) {
				uisContainer.removeItem(user);
			}
		}
		uisTreeTable.sanitizeSelection();
	}

	/**
	 * Reçoit une notification d'UI ajoutée
	 * @param ui
	 */
	public void receiveAddedUINotification(final UI ui) {
		getUI().access(() -> {
			addUI(ui);
			uisContainer.sort(new Object[]{PROPERTY_ID}, new boolean[]{true});
			updateClientTreeTableTitle();
		});
	}

	/**
	 * Reçoit une notification d'UI modifée
	 * @param ui
	 */
	public void receiveUpdatedUINotification(final UI ui) {
		getUI().access(() -> {
			updateUI(ui);
		});
	}

	/**
	 * Reçoit une notification d'UI supprimée
	 * @param ui
	 */
	public void receiveRemovedUINotification(final UI ui) {
		getUI().access(() -> {
			removeUI(ui);
			updateClientTreeTableTitle();
		});
	}

	private void updateClientTreeTableTitle() {
		clientTreeTableTitle.setValue(applicationContext.getMessage("admin.uiList.title", new Object[] {nbClients}, getLocale()));
	}

}
