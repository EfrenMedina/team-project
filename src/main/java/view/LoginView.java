package view;

import interfaceadapter.ViewNames;
import interfaceadapter.navigation.NavigationController;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Login and sign-up screen (wireframe 01), laid out as designed but not wired up: either button
 * goes straight to the dashboard. US-01 and US-02 connect it to the sign-up and login use cases.
 */
public class LoginView extends JPanel {

  private static final String LOG_IN = "Log in";
  private static final String SIGN_UP = "Sign up";
  private static final int CARD_WIDTH = 280;
  private static final int FIELD_WIDTH = 232;

  private final CardLayout formLayout = new CardLayout();
  private final JPanel forms = new JPanel(formLayout);
  private final JButton logInTab = segment(LOG_IN);
  private final JButton signUpTab = segment(SIGN_UP);

  /**
   * Builds the screen.
   *
   * @param navigation used to move to the dashboard
   */
  public LoginView(NavigationController navigation) {
    super(new BorderLayout());
    setName(ViewNames.LOGIN);
    setBackground(Theme.BACKGROUND);
    setBorder(BorderFactory.createEmptyBorder(96, 96, 96, 96));

    JPanel logoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    logoRow.setOpaque(false);
    logoRow.add(new Logo());
    add(logoRow, BorderLayout.NORTH);

    JPanel body = new JPanel(new GridBagLayout());
    body.setOpaque(false);
    GridBagConstraints c = new GridBagConstraints();
    c.gridy = 0;
    c.weightx = 1;
    c.anchor = GridBagConstraints.WEST;
    body.add(tagline(), c);
    c.weightx = 0;
    c.anchor = GridBagConstraints.EAST;
    c.insets = new Insets(0, 0, 0, 216);
    body.add(card(navigation), c);
    add(body, BorderLayout.CENTER);
  }

  private static JComponent tagline() {
    JPanel panel = column();
    panel.add(Theme.label("Practise investing", 40, true, Theme.TEXT));
    panel.add(Box.createVerticalStrut(16));
    panel.add(Theme.h2("with $10,000.00 of simulated money."));
    panel.add(Box.createVerticalStrut(16));
    panel.add(Theme.small("Real prices. No real risk."));
    return panel;
  }

  private JComponent card(NavigationController navigation) {
    forms.setOpaque(false);
    forms.setAlignmentX(LEFT_ALIGNMENT);
    forms.add(logInForm(navigation), LOG_IN);
    forms.add(signUpForm(navigation), SIGN_UP);

    logInTab.addActionListener(e -> showForm(LOG_IN));
    signUpTab.addActionListener(e -> showForm(SIGN_UP));
    RoundedPanel tabs = new RoundedPanel(new GridLayout(1, 2, 4, 0), Theme.INPUT, 8);
    tabs.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    tabs.add(logInTab);
    tabs.add(signUpTab);
    tabs.setAlignmentX(LEFT_ALIGNMENT);
    tabs.setPreferredSize(new Dimension(FIELD_WIDTH, 36));
    tabs.setMaximumSize(new Dimension(FIELD_WIDTH, 36));
    showForm(LOG_IN);

    JPanel content = column();
    content.add(tabs);
    content.add(Box.createVerticalStrut(24));
    content.add(forms);

    RoundedPanel card = new RoundedPanel(new BorderLayout(), Theme.SURFACE, 16);
    card.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
    card.setPreferredSize(new Dimension(CARD_WIDTH, 460));
    card.add(content, BorderLayout.NORTH);
    return card;
  }

  private void showForm(String name) {
    formLayout.show(forms, name);
    styleSegment(logInTab, LOG_IN.equals(name));
    styleSegment(signUpTab, SIGN_UP.equals(name));
  }

  private JComponent logInForm(NavigationController navigation) {
    JPanel form = column();
    addField(form, "Username", Theme.textField());
    addField(form, "Password", Theme.passwordField());
    form.add(Box.createVerticalStrut(12));
    JButton logIn = Theme.primaryButton(LOG_IN);
    logIn.addActionListener(e -> navigation.navigateTo(ViewNames.DASHBOARD));
    form.add(fullWidth(logIn, 44));
    form.add(Box.createVerticalStrut(20));
    JButton switchToSignUp = Theme.linkButton("New here? Sign up", 12, true, Theme.MUTED);
    switchToSignUp.addActionListener(e -> showForm(SIGN_UP));
    form.add(switchToSignUp);
    return form;
  }

  private JComponent signUpForm(NavigationController navigation) {
    JPanel form = column();
    addField(form, "Username", Theme.textField());
    addField(form, "Password", Theme.passwordField());
    addField(form, "Confirm password", Theme.passwordField());
    form.add(Theme.footnote("At least 8 characters."));
    form.add(Box.createVerticalStrut(16));
    JButton create = Theme.primaryButton("Create account");
    create.addActionListener(e -> navigation.navigateTo(ViewNames.DASHBOARD));
    form.add(fullWidth(create, 44));
    form.add(Box.createVerticalStrut(20));
    form.add(Theme.small("New accounts start with $10,000.00 cash."));
    return form;
  }

  private static void addField(JPanel form, String label, JComponent field) {
    form.add(Theme.small(label));
    form.add(Box.createVerticalStrut(6));
    form.add(fullWidth(field, 40));
    form.add(Box.createVerticalStrut(14));
  }

  private static JComponent fullWidth(JComponent component, int height) {
    component.setAlignmentX(LEFT_ALIGNMENT);
    Dimension size = new Dimension(FIELD_WIDTH, height);
    component.setPreferredSize(size);
    component.setMaximumSize(size);
    return component;
  }

  private static JButton segment(String text) {
    JButton button = Theme.flatButton(text, false);
    button.setHorizontalAlignment(JButton.CENTER);
    return button;
  }

  private static void styleSegment(JButton button, boolean selected) {
    button.setFont(Theme.font(14, selected));
    button.setForeground(selected ? Theme.TEXT : Theme.MUTED);
    button.setBackground(selected ? Theme.RAISED : Theme.INPUT);
  }

  private static JPanel column() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setOpaque(false);
    panel.setAlignmentX(LEFT_ALIGNMENT);
    return panel;
  }
}
