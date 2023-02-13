import java.awt.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.geom.Rectangle2D;
import java.awt.event.*;
import java.io.*;
//Класс для отображения фрактала
public class FractalExplorer {
    private int displaySize;
    //Константы, хардкоженные строки
    private static final String TITLE = "Навигатор фракталов";
    private static final String RESET = "Сброс";
    private static final String SAVE = "Сохранить";
    private static final String CHOOSE = "Выберете фрактал :";
    private JImageDisplay display;
    private FractalGenerator fractal;
    private Rectangle2D.Double range;
    private JComboBox<FractalGenerator> comboBox;

    //Имплементируем интерфейс ActionListener для кнопки сброса
    class ActionHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String cmd = e.getActionCommand(); // Получаем команду обработчика событий
            if (RESET.equals(cmd)){
                fractal.getInitialRange(range);
                drawFractal();
            } else if (SAVE.equals(cmd)) {
                JFileChooser fileChooser = new JFileChooser(); // Создали
                FileNameExtensionFilter filter = new FileNameExtensionFilter("PNG Images", "png"); // добавили фильтр, чтобы программа принимала только пнг формат
                fileChooser.setFileFilter(filter); // привязываем фильтр
                fileChooser.setAcceptAllFileFilterUsed(false);
                if (fileChooser.showSaveDialog(display) == JFileChooser.APPROVE_OPTION){ // Если человек согласился сохранить
                    File file = fileChooser.getSelectedFile(); // Получаем настройки файла
                    String path = file.toString(); // Получаем путь
                    if (path.length() == 0) return; // Проверяем путь. Не даем сохранить нулевую длину
                    if (!path.contains(".png")) file = new File(path + ".png"); // Проверяем разрешение файла
                    try {
                        javax.imageio.ImageIO.write(display.getImage(), "png", file); // Пробуем сохранить
                    } catch (Exception exception){
                        JOptionPane.showMessageDialog(display, exception.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE); // Выдаем ошибку
                    }
                }
            } else if ("comboBoxChanged".equals(cmd)) {  // Обрабатываем события от селектора
                fractal = (FractalGenerator) comboBox.getSelectedItem();
                fractal.getInitialRange(range);
                drawFractal();
            }
        }
    }
    //Наследуем MouseAdapter для обработки событий мыши
    class MouseHandler extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            display.clearImage();
            int x = e.getX();
            double xCoord = FractalGenerator.getCoord(range.x, range.x + range.width, displaySize, x);
            int y = e.getY();
            double yCoord = FractalGenerator.getCoord(range.y, range.y + range.height, displaySize, y);
            fractal.recenterAndZoomRange(range, xCoord, yCoord, 0.5);
            drawFractal();
        }
    }

    //Точка входа в программу
    public static void main(String[] args){
        FractalExplorer fractalExplorer = new FractalExplorer(600);
        fractalExplorer.createAndShowGUI();
    }

    //Конструктор класса
    public FractalExplorer(int displaySize){
        this.displaySize = displaySize;
        fractal = new Mandelbrot();
        range = new Rectangle2D.Double();
        fractal.getInitialRange(range); // задаём начальный диапазон
    }

    //Метод для инициализации графического интерфейса Swing
    public void createAndShowGUI(){
        JFrame frame = new JFrame(TITLE);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        display = new JImageDisplay(displaySize, displaySize);
        frame.add(display, BorderLayout.CENTER);
        ActionHandler handler = new ActionHandler();

        JPanel top = new JPanel(); // создаем первую (верхнюю) панель
        JPanel bottom = new JPanel();  //создаём вторую (нижнюю) панель

        JButton save = new JButton(SAVE);
        save.addActionListener(handler); // мы добавляем обработчик событий для кнопки сохранить

        JButton resetButton = new JButton(RESET);

        resetButton.addActionListener(handler);

        bottom.add(save, BorderLayout.WEST);  // Указываем местоположение кнопки "Сохранить"
        bottom.add(resetButton, BorderLayout.EAST); // Указываем местоположение кнопки "Сброс"

        JLabel label = new JLabel(CHOOSE);   // Создаем подпись
        top.add(label, BorderLayout.WEST);   // Привязываем местоположение подписи

        comboBox = new JComboBox<FractalGenerator>();           // Создали селектор переключения между фракталами
        comboBox.addItem(new Mandelbrot());
        comboBox.addItem(new BurningShip());
        comboBox.addItem(new Tricorn());
        comboBox.addActionListener(handler); // Добавляем обработчик событий для селектора
        top.add(comboBox, BorderLayout.EAST);  // Указали местоположение кнопки селектора



        MouseHandler click = new MouseHandler();
        display.addMouseListener(click);

        frame.add(top, BorderLayout.NORTH);
        frame.add(bottom, BorderLayout.SOUTH);  // Привязываем нижнюю панель к низу
        frame.pack();
        frame.setVisible(true);
        frame.setResizable(false); // Запрещаем изменение экрана
        drawFractal();
    }

    //Метод для отрисовки фрактала
    private void drawFractal(){
        for(int i = 0; i < displaySize; i++){
            for(int j = 0; j < displaySize; j++){
                double xCoord = fractal.getCoord(range.x, range.x + range.width, displaySize, i); //Получаем координаты фрактала по координатам дисплея
                double yCoord = fractal.getCoord(range.y, range.y + range.height, displaySize, j);
                int iteration = fractal.numIterations(xCoord, yCoord); // ПОлучаем количество иттераций для определенной точки по координатам фрактала.
                if (iteration == -1) {
                    display.drawPixel(i, j, 0);
                }
                else {
                    float hue = 0.7f + (float) iteration / 200f;
                    int rgbColor = Color.HSBtoRGB(hue, 1f, 1f);
                    display.drawPixel(i, j, rgbColor);
                }
            }
            display.repaint();
        }
    }
}