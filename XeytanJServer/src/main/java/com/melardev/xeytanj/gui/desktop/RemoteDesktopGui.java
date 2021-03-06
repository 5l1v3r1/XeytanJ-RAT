package com.melardev.xeytanj.gui.desktop;

import com.melardev.xeytanj.enums.NetworkProtocol;
import com.melardev.xeytanj.enums.ServiceType;
import com.melardev.xeytanj.gui.IGuiUserOwned;
import com.melardev.xeytanj.gui.mediator.IUiMediator;
import com.melardev.xeytanj.models.Client;
import com.melardev.xeytanj.models.ScreenDeviceInfo;
import com.melardev.xeytanj.services.IAppMessageProvider;
import net.miginfocom.swing.MigLayout;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.UUID;

@Component
public class RemoteDesktopGui extends JFrame implements ActionListener, ChangeListener, IGuiUserOwned<RdpUiListener> {
    private IUiMediator mediator;
    private RdpUiListener listesner;
    private UUID id;
    private Icon pauseIcon;
    private Icon stopIcon;
    private Icon playIcon;
    private Icon helpIcon;
    private IAppMessageProvider messageProvider;
    private JPanel mainPanel;
    private JRadioButton rdbtUDP;
    private Client client;

    @Override
    public void display() {
        pauseIcon = new ImageIcon(getClass().getClassLoader().getResource("icons/utils/pause.png"));
        stopIcon = new ImageIcon(RemoteDesktopGui.class.getResource("/icons/utils/stop_32.png"));
        playIcon = new ImageIcon(RemoteDesktopGui.class.getResource("/icons/utils/play.png"));
        helpIcon = new ImageIcon(RemoteDesktopGui.class.getResource("/icons/utils/help_32.png"));

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                notifyMediatorOnClose();
            }
        });
        getContentPane().setLayout(new MigLayout("", "[left][589.00px,growprio 110,grow,fill][]", "[25.00px][369.00,grow]"));

        mainPanel = new JPanel();
        getContentPane().add(mainPanel, "cell 0 1,grow");
        mainPanel.setLayout(new MigLayout("", "[50px][200px][114px,grow]", "[23px][45px][46px][41px][][]"));

        JLabel lblDisplay = new JLabel("Display");
        mainPanel.add(lblDisplay, "cell 0 0,alignx center,aligny center");

        comboDisplay = new JComboBox();
        mainPanel.add(comboDisplay, "cell 1 0,growx,aligny center");

        JLabel lblDelay = new JLabel("delay (ms)");
        mainPanel.add(lblDelay, "cell 0 1,alignx center,aligny center");

        sliderDelay = new JSlider();
        sliderDelay.setMinorTickSpacing(500);
        sliderDelay.setMajorTickSpacing(1000);
        sliderDelay.setPaintTicks(true);
        sliderDelay.setPaintLabels(true);
        sliderDelay.setMaximum(3000);
        sliderDelay.addChangeListener(this);
        mainPanel.add(sliderDelay, "cell 1 1,alignx left,aligny top");

        txtDelay = new JTextField();
        mainPanel.add(txtDelay, "cell 2 1,alignx center,aligny center");
        txtDelay.setColumns(10);
        txtDelay.setText(String.valueOf(sliderDelay.getValue()));

        Box verticalBox = Box.createVerticalBox();
        mainPanel.add(verticalBox, "cell 1 2,growx,aligny top");

        rdbtnTCP = new JRadioButton("TCP");
        rdbtnTCP.setSelected(true);
        verticalBox.add(rdbtnTCP);

        rdbtUDP = new JRadioButton("UDP");
        verticalBox.add(rdbtUDP);
        rdbtUDP.setEnabled(false);

        lblResizeImagefactor = new JLabel("Resize X");
        mainPanel.add(lblResizeImagefactor, "cell 0 3");

        sliderScaleX = new JSlider();
        sliderScaleX.setValue(100);
        sliderScaleX.setMajorTickSpacing(50);
        sliderScaleX.setMinorTickSpacing(20);
        sliderScaleX.setSnapToTicks(true);
        sliderScaleX.setPaintTicks(true);
        sliderScaleX.setPaintLabels(true);
        mainPanel.add(sliderScaleX, "cell 1 3,aligny center");
        lblScaleX = new JLabel();
        sliderScaleX.addChangeListener(this);
        lblScaleX.setText("100%");
        mainPanel.add(lblScaleX, "cell 2 3,alignx center,aligny center");

        lblResizeY = new JLabel("Resize Y");
        mainPanel.add(lblResizeY, "cell 0 4");

        sliderScaleY = new JSlider();
        sliderScaleY.setSnapToTicks(true);
        sliderScaleY.setMinorTickSpacing(20);
        sliderScaleY.setMajorTickSpacing(50);
        sliderScaleY.setPaintLabels(true);
        sliderScaleY.setPaintTicks(true);
        sliderScaleY.setValue(100);
        mainPanel.add(sliderScaleY, "cell 1 4");
        sliderScaleY.addChangeListener(this);
        lblScaleY = new JLabel("100%");
        mainPanel.add(lblScaleY, "cell 2 4,alignx center");

        Box horizontalBox = Box.createHorizontalBox();
        mainPanel.add(horizontalBox, "cell 1 5,alignx center,aligny center");

        btnGo = new JButton("Go");
        horizontalBox.add(btnGo);
        btnGo.setIcon(playIcon);
        btnGo.addActionListener(this);

        /*btnStop = new JButton("Stop");
        btnStop.setIcon(stopIcon);
        horizontalBox.add(btnStop);
        btnStop.addActionListener(this);
*/
        btnHelp = new JButton("Help");
        btnHelp.setIcon(helpIcon);
        mainPanel.add(btnHelp, "cell 2 5,growx,aligny top");
        btnHelp.addActionListener(this);


        lblImage = new JLabel("");
        scrollPane = new JScrollPane(lblImage);
        getContentPane().add(scrollPane, "cell 1 0 1 2,grow");
        scrollPane.getVerticalScrollBar().setUnitIncrement(100);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(100);
        setSize(1095, 525);
        setVisible(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    @Override
    public void addListener(RdpUiListener listener) {
        this.listesner = listener;
    }

    @Override
    public RdpUiListener getListener() {
        return listesner;
    }


    @Override
    public Client getClient() {
        return client;
    }

    @Override
    public void setClient(Client client) {
        this.client = client;
        setTitle("Remote Desktop " + client.getPcName());
    }

    @Override
    public ServiceType getServiceType() {
        return ServiceType.REMOTE_DESKTOP;
    }

    @Override
    public void setStatus(String status) {

    }

    @Override
    public void disableUi() {
        mainPanel.setEnabled(false);
        sliderScaleX.setEnabled(false);
        sliderScaleY.setEnabled(false);
        sliderDelay.setEnabled(false);
        txtDelay.setEnabled(false);

        rdbtnTCP.setEnabled(false);
        rdbtUDP.setEnabled(false);
        comboDisplay.setEnabled(false);

        btnGo.setEnabled(false);
        setTitle("User disconnected " + client.getPcName());

    }

    @Override
    public IUiMediator getMediator() {
        return mediator;
    }

    @Override
    public void setMediator(IUiMediator mediator) {
        this.mediator = mediator;
    }

    @Override
    public void resetState() {

    }

    public void setDesktopImage(ImageIcon image) {
        this.lblImage.setIcon(image);
    }


    private class ConfigFrame extends JFrame {

    }

    private ObjectInputStream sockIn;
    private ObjectOutputStream sockOut;
    private ConfigFrame frameConfig;
    private JLabel lblImage;

    private JTextField txtDelay;
    private JComboBox comboDisplay;
    private JButton btnGo;
    //private JButton btnStop;
    private JButton btnHelp;
    private JSlider sliderDelay;
    private JRadioButton rdbtnTCP;
    private JScrollPane scrollPane;
    private JSlider sliderScaleX;
    private JLabel lblResizeImagefactor;
    private JLabel lblScaleX;
    private JLabel lblScaleY;
    private JSlider sliderScaleY;
    private JLabel lblResizeY;

    public void initializeGUI() {

    }

    public RemoteDesktopGui() {
    }

    public void drawDesktop(ImageIcon image) {
        lblImage.setIcon(image);

    }

    public void setConfig(List<ScreenDeviceInfo> screenDeviceInfoList) {
        for (ScreenDeviceInfo screenDevice : screenDeviceInfoList) {
            comboDisplay.addItem(screenDevice.getDeviceName());
        }

        if (screenDeviceInfoList.size() > 1)
            comboDisplay.addItem("AllDisplays");
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        Object source = evt.getSource();
        // MediaClientInnerConnection handler = (MediaClientInnerConnection) serverHandler;

        if (source == btnGo) {
            updateBtnGoAndFire();
            //} else if (source == btnStop) {
            //  stopClicked();
        } else if (source == btnHelp) {
            helpClicked();
        }
    }

    private void helpClicked() {

    }

    private void stopClicked() {
        dispose();
    }

    private void updateBtnGoAndFire() {
        if (btnGo.getText().equalsIgnoreCase("Go") || btnGo.getText().equalsIgnoreCase("play")) {

            btnGo.setText("Pause");
            btnGo.setIcon(pauseIcon);
            int scaleX = sliderScaleX.getValue();
            int scaleY = sliderScaleY.getValue();

            if (scaleX < 10)
                scaleX = 10;
            if (scaleY < 10)
                scaleY = 10;

            getListener().onRdpPlayBtnClicked(client, rdbtnTCP.isSelected() ? NetworkProtocol.TCP : NetworkProtocol.UDP, comboDisplay.getSelectedItem().toString(), scaleX, scaleY, Integer.parseInt(txtDelay.getText()));
        } else {
            btnGo.setText("Play");
            btnGo.setIcon(playIcon);
            getListener().onRdpPauseBtnClicked(client);
        }
    }

    @Override
    public void stateChanged(ChangeEvent evt) {
        Object source = evt.getSource();
        if (source == sliderDelay)
            txtDelay.setText(String.valueOf(sliderDelay.getValue()));
        else if (source == sliderScaleX)
            lblScaleX.setText(String.valueOf(sliderScaleX.getValue() + "%"));
        else if (source == sliderScaleY)
            lblScaleY.setText(String.valueOf(sliderScaleY.getValue() + "%"));
    }

    @Override
    public void setMessageProvider(IAppMessageProvider messageProvider) {
        this.messageProvider = messageProvider;
    }

}
