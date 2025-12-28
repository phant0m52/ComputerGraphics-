package ui;

import engine.Mesh;
import math.MathUtil;
import math.Vec3;

import javax.swing.*;
import java.awt.*;

public final class TransformPanel extends JPanel {
    private final RenderPanel renderPanel;

    public TransformPanel(RenderPanel renderPanel) {
        this.renderPanel = renderPanel;
        setLayout(new BorderLayout());
        add(buildControls(), BorderLayout.NORTH);
    }

    private JPanel buildControls() {
        JPanel p = new JPanel(new GridLayout(0, 4, 6, 6));
        p.setBorder(BorderFactory.createTitledBorder("Model Transform (TRS)"));

        // Translation
        JSpinner tx = spinner(0), ty = spinner(0), tz = spinner(0);
        // Rotation (degrees)
        JSpinner rx = spinner(0), ry = spinner(0), rz = spinner(0);
        // Scale
        JSpinner sx = spinner(1), sy = spinner(1), sz = spinner(1);

        addRow(p, "T.x", tx, "T.y", ty);
        addRow(p, "T.z", tz, "", null);
        addRow(p, "R.x°", rx, "R.y°", ry);
        addRow(p, "R.z°", rz, "", null);
        addRow(p, "S.x", sx, "S.y", sy);
        addRow(p, "S.z", sz, "", null);

        Runnable apply = () -> {
            Mesh m = renderPanel.getMesh();
            if (m == null) return;

            double Ttx = (double) tx.getValue();
            double Tty = (double) ty.getValue();
            double Ttz = (double) tz.getValue();

            double Rrx = MathUtil.degToRad((double) rx.getValue());
            double Rry = MathUtil.degToRad((double) ry.getValue());
            double Rrz = MathUtil.degToRad((double) rz.getValue());

            double Ssx = (double) sx.getValue();
            double Ssy = (double) sy.getValue();
            double Ssz = (double) sz.getValue();

            m.transform.setPosition(Vec3.of(Ttx, Tty, Ttz));
            m.transform.setRotationRad(Vec3.of(Rrx, Rry, Rrz));
            m.transform.setScale(Vec3.of(Ssx, Ssy, Ssz));

            renderPanel.repaint();
        };

        // слушатели
        for (JSpinner sp : new JSpinner[]{tx,ty,tz,rx,ry,rz,sx,sy,sz}) {
            sp.addChangeListener(e -> apply.run());
        }

        JButton reset = new JButton("Reset");
        reset.addActionListener(e -> {
            tx.setValue(0.0); ty.setValue(0.0); tz.setValue(0.0);
            rx.setValue(0.0); ry.setValue(0.0); rz.setValue(0.0);
            sx.setValue(1.0); sy.setValue(1.0); sz.setValue(1.0);
        });

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.add(p, BorderLayout.CENTER);
        wrap.add(reset, BorderLayout.SOUTH);
        return wrap;
    }

    private static void addRow(JPanel p, String l1, JSpinner s1, String l2, JSpinner s2) {
        p.add(new JLabel(l1));
        p.add(s1);
        p.add(new JLabel(l2));
        p.add(s2 == null ? new JLabel("") : s2);
    }

    private static JSpinner spinner(double initial) {
        SpinnerNumberModel model = new SpinnerNumberModel(initial, -1e6, 1e6, 0.1);
        JSpinner sp = new JSpinner(model);
        JSpinner.NumberEditor ed = new JSpinner.NumberEditor(sp, "0.###");
        sp.setEditor(ed);
        return sp;
    }
}
