import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.table.*;
import java.util.List;

public class ChatCalendar extends JPanel {
    private JLabel monthLabel;
    private JTable calendar;
    private DefaultTableModel model;
    private JButton prevButton, nextButton, addEventButton;
    private Calendar currentCalendar;
    private Map<String, java.util.List<String>> events; // 날짜별 이벤트 저장 (format: "YYYY-MM-DD")
    private Set<String> categories; // 카테고리 목록
    private Map<String, String> eventCategories; // 이벤트별 카테고리 저장
    private Set<String> selectedCategories; // 현재 선택된 카테고리들
    private JPanel categoryPanel; // 카테고리 체크박스들을 담을 패널

    public ChatCalendar() {
        categories = new HashSet<>();
        categories.add("일반"); // 기본 카테고리
        eventCategories = new HashMap<>();
        selectedCategories = new HashSet<>();
        selectedCategories.add("일반"); // 기본 카테고리는 선택된 상태로 시작

        events = new HashMap<>();
        currentCalendar = Calendar.getInstance();
        setLayout(new BorderLayout());
        setupCalendarPanel();
        setupCategoryPanel();
        updateCalendar();
    }

    private void setupCategoryPanel() {
        categoryPanel = new JPanel();
        categoryPanel.setLayout(new BorderLayout());

        // 카테고리 체크박스를 담을 패널
        JPanel checkboxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // 카테고리 관리 버튼을 담을 패널
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // 카테고리 추가 버튼
        JButton addCategoryButton = new JButton("카테고리 추가");
        addCategoryButton.addActionListener(e -> showAddCategoryDialog());

        // 카테고리 삭제 버튼
        JButton deleteCategoryButton = new JButton("카테고리 삭제");
        deleteCategoryButton.addActionListener(e -> showDeleteCategoryDialog());

        buttonPanel.add(addCategoryButton);
        buttonPanel.add(deleteCategoryButton);

        categoryPanel.add(checkboxPanel, BorderLayout.CENTER);
        categoryPanel.add(buttonPanel, BorderLayout.EAST);
        add(categoryPanel, BorderLayout.SOUTH);

        updateCategoryCheckboxes(checkboxPanel);
    }

    private void updateCategoryCheckboxes(JPanel checkboxPanel) {
        checkboxPanel.removeAll();
        for (String category : categories) {
            JCheckBox checkBox = new JCheckBox(category);
            checkBox.setSelected(selectedCategories.contains(category));
            checkBox.addActionListener(e -> {
                if (checkBox.isSelected()) {
                    selectedCategories.add(category);
                } else {
                    selectedCategories.remove(category);
                }
                updateCalendar();
            });
            checkboxPanel.add(checkBox);
        }
        checkboxPanel.revalidate();
        checkboxPanel.repaint();
    }

    // 카테고리 추가 다이얼로그
    private void showAddCategoryDialog() {
        String newCategory = JOptionPane.showInputDialog(this,
                "새 카테고리 이름을 입력하세요:",
                "카테고리 추가",
                JOptionPane.PLAIN_MESSAGE);

        if (newCategory != null && !newCategory.trim().isEmpty()) {
            newCategory = newCategory.trim();
            if (!categories.contains(newCategory)) {
                categories.add(newCategory);
                selectedCategories.add(newCategory);
                updateCategoryCheckboxes((JPanel) categoryPanel.getComponent(0));
                updateCalendar();
            } else {
                JOptionPane.showMessageDialog(this,
                        "이미 존재하는 카테고리입니다.",
                        "오류",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // 카테고리 삭제 다이얼로그
    private void showDeleteCategoryDialog() {
        if (categories.size() <= 1) {
            JOptionPane.showMessageDialog(this,
                    "삭제할 수 있는 카테고리가 없습니다.",
                    "오류",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        Object[] options = categories.stream()
                .filter(c -> !c.equals("일반"))
                .toArray();

        if (options.length == 0) {
            JOptionPane.showMessageDialog(this,
                    "삭제할 수 있는 카테고리가 없습니다.",
                    "오류",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String selectedCategory = (String) JOptionPane.showInputDialog(this,
                "삭제할 카테고리를 선택하세요:",
                "카테고리 삭제",
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (selectedCategory != null) {
            // 해당 카테고리의 모든 이벤트를 '일반' 카테고리로 이동
            for (Map.Entry<String, String> entry : new HashMap<>(eventCategories).entrySet()) {
                if (entry.getValue().equals(selectedCategory)) {
                    eventCategories.put(entry.getKey(), "일반");
                }
            }

            categories.remove(selectedCategory);
            selectedCategories.remove(selectedCategory);
            updateCategoryCheckboxes((JPanel) categoryPanel.getComponent(0));
            updateCalendar();
        }
    }

    private void setupCalendarPanel() {
        // 상단 패널 (월 표시 및 버튼)
        JPanel controlPanel = new JPanel(new FlowLayout());
        prevButton = new JButton("◀");
        nextButton = new JButton("▶");
        monthLabel = new JLabel("", SwingConstants.CENTER);
        addEventButton = new JButton("이벤트 추가");

        controlPanel.add(prevButton);
        controlPanel.add(monthLabel);
        controlPanel.add(nextButton);
        controlPanel.add(addEventButton);

        // 이벤트 삭제 버튼 추가
        JButton deleteEventButton = new JButton("이벤트 삭제");
        deleteEventButton.addActionListener(e -> showDeleteEventDialog());
        controlPanel.add(deleteEventButton);

        // 캘린더 테이블 설정
        String[] columnNames = { "일", "월", "화", "수", "목", "금", "토" };
        model = new DefaultTableModel(null, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        calendar = new JTable(model) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component comp = super.prepareRenderer(renderer, row, col);
                // 테이블의 전체 높이를 계산하여 각 행의 높이 설정
                int height = getParent().getHeight();
                int rowCount = getRowCount();
                if (rowCount > 0) {
                    setRowHeight(height / rowCount);
                }
                return comp;
            }
        };

        // 테이블 설정
        calendar.setFillsViewportHeight(true);
        calendar.setShowGrid(true);
        calendar.setGridColor(Color.GRAY);
        calendar.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // 헤더 정렬 설정 (오른쪽 정렬)
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                        column);
                label.setHorizontalAlignment(JLabel.RIGHT);
                label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
                label.setFont(label.getFont().deriveFont(Font.BOLD, 14));
                return label;
            }
        };
        calendar.getTableHeader().setDefaultRenderer(headerRenderer);

        // 헤더 테두리 설정
        calendar.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));

        // 셀 내용 렌더러 설정
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JPanel panel = new JPanel(new BorderLayout(5, 5));

                // 오늘 날짜인지 확인하고 배경색 설정
                if (value != null && !value.toString().isEmpty()) {
                    String[] parts = value.toString().split("\n");
                    Calendar today = Calendar.getInstance();
                    if (today.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
                            today.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH) &&
                            today.get(Calendar.DAY_OF_MONTH) == Integer.parseInt(parts[0])) {
                        panel.setBackground(new Color(255, 192, 203)); // 핑크색 배경
                    } else {
                        panel.setBackground(table.getBackground());
                    }

                    // 날짜 숫자 (오른쪽 상단)
                    JLabel dateLabel = new JLabel(parts[0]);
                    dateLabel.setHorizontalAlignment(JLabel.RIGHT);
                    dateLabel.setVerticalAlignment(JLabel.TOP);
                    dateLabel.setFont(dateLabel.getFont().deriveFont(Font.BOLD, 16));
                    panel.add(dateLabel, BorderLayout.NORTH);

                    // 이벤트가 있는 경우
                    if (parts.length > 1) {
                        // 이벤트들을 담을 패널
                        JPanel eventsPanel = new JPanel();
                        eventsPanel.setLayout(new BoxLayout(eventsPanel, BoxLayout.Y_AXIS));
                        eventsPanel.setBackground(panel.getBackground()); // 부모 패널과 같은 배경색 설정

                        // 첫 번째 이벤트를 제외한 나머지 이벤트들
                        for (int i = 1; i < parts.length; i++) {
                            JLabel eventLabel = new JLabel(parts[i]);
                            eventLabel.setHorizontalAlignment(JLabel.RIGHT);
                            eventLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
                            eventLabel.setFont(eventLabel.getFont().deriveFont(Font.PLAIN, 10));
                            eventsPanel.add(eventLabel);
                        }
                        panel.add(eventsPanel, BorderLayout.CENTER);
                    }
                } else {
                    panel.setBackground(table.getBackground());
                }

                panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 5));
                return panel;
            }
        };
        calendar.setDefaultRenderer(Object.class, cellRenderer);

        // 컬럼 너비를 동일하게 설정
        TableColumnModel columnModel = calendar.getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            TableColumn column = columnModel.getColumn(i);
            column.setMinWidth(50);
        }

        // 스크롤 패널에 테이블 추가
        JScrollPane scrollPane = new JScrollPane(calendar) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(0, 0); // 스크롤 패널이 부모 컨테이너를 채우도록 함
            }
        };
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // 버튼 이벤트 설정
        prevButton.addActionListener(e -> {
            currentCalendar.add(Calendar.MONTH, -1);
            updateCalendar();
        });

        nextButton.addActionListener(e -> {
            currentCalendar.add(Calendar.MONTH, 1);
            updateCalendar();
        });

        addEventButton.addActionListener(e -> showAddEventDialog());

        // 레이아웃 설정
        setLayout(new BorderLayout());
        add(controlPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // 컴포넌트 리스너 추가
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                calendar.revalidate();
                calendar.repaint();
            }
        });
    }

    private void updateCalendar() {
        // 달력 초기화
        model.setRowCount(0);

        // 현재 년월 표시
        int year = currentCalendar.get(Calendar.YEAR);
        int month = currentCalendar.get(Calendar.MONTH);
        monthLabel.setText(String.format("%d년 %d월", year, month + 1));

        // 달력 데이터 생성
        Calendar cal = (Calendar) currentCalendar.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int startDay = cal.get(Calendar.DAY_OF_WEEK);
        int numberOfDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        int rows = 6;
        Object[][] calendarData = new Object[rows][7];
        int day = 1;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < 7; j++) {
                if (i == 0 && j < startDay - 1) {
                    calendarData[i][j] = "";
                } else if (day > numberOfDays) {
                    calendarData[i][j] = "";
                } else {
                    String dateKey = String.format("%d-%02d-%02d", year, month + 1, day);
                    StringBuilder cellContent = new StringBuilder(String.valueOf(day));
                    if (events.containsKey(dateKey)) {
                        for (String event : events.get(dateKey)) {
                            String eventCategory = eventCategories.get(dateKey + "|" + event);
                            if (selectedCategories.contains(eventCategory)) {
                                cellContent.append("\n").append(event);
                            }
                        }
                    }
                    calendarData[i][j] = cellContent.toString();
                    day++;
                }
            }
        }

        for (Object[] row : calendarData) {
            model.addRow(row);
        }
    }

    private void showAddEventDialog() {
        JTextField eventNameField = new JTextField(20);
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setValue(new Date());

        // 카테고리 선택/생성을 위한 컴포넌트들
        JComboBox<String> categoryComboBox = new JComboBox<>(categories.toArray(new String[0]));
        categoryComboBox.setEditable(true);

        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.add(new JLabel("이벤트 이름:"));
        panel.add(eventNameField);
        panel.add(new JLabel("날짜:"));
        panel.add(dateSpinner);
        panel.add(new JLabel("카테고리 (선택 또는 새로 입력):"));
        panel.add(categoryComboBox);

        int result = JOptionPane.showConfirmDialog(this, panel, "이벤트 추가",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String eventName = eventNameField.getText().trim();
            Date selectedDate = (Date) dateSpinner.getValue();
            String category = categoryComboBox.getSelectedItem().toString().trim();

            if (!category.isEmpty()) {
                categories.add(category); // 새 카테고리 추가
                selectedCategories.add(category); // 새 카테고리 자동 선택
                updateCategoryCheckboxes((JPanel) categoryPanel.getComponent(0));
            }

            Calendar cal = Calendar.getInstance();
            cal.setTime(selectedDate);

            String dateKey = String.format("%d-%02d-%02d",
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH) + 1,
                    cal.get(Calendar.DAY_OF_MONTH));

            events.computeIfAbsent(dateKey, k -> new ArrayList<>()).add(eventName);
            eventCategories.put(dateKey + "|" + eventName, category);
            updateCalendar();
        }
    }

    private void showDeleteEventDialog() {
        // 현재 선택된 날짜의 이벤트 목록 가져오기
        Calendar cal = Calendar.getInstance();
        String currentDate = String.format("%d-%02d-%02d",
                currentCalendar.get(Calendar.YEAR),
                currentCalendar.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH));

        List<String> dateEvents = events.get(currentDate);
        if (dateEvents == null || dateEvents.isEmpty()) {
            JOptionPane.showMessageDialog(this, "삭제할 이벤트가 없습니다.");
            return;
        }

        // 이벤트 선택을 위한 콤보박스
        JComboBox<String> eventComboBox = new JComboBox<>(dateEvents.toArray(new String[0]));

        int result = JOptionPane.showConfirmDialog(this, eventComboBox, "삭제할 이벤트 선택",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String selectedEvent = (String) eventComboBox.getSelectedItem();
            dateEvents.remove(selectedEvent);
            eventCategories.remove(currentDate + "|" + selectedEvent);
            if (dateEvents.isEmpty()) {
                events.remove(currentDate);
            }
            updateCalendar();
        }
    }

    // 이벤트 데이터를 문자열로 변환 (서버 전송용)
    public String getEventsAsString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, List<String>> entry : events.entrySet()) {
            for (String event : entry.getValue()) {
                String category = eventCategories.get(entry.getKey() + "|" + event);
                sb.append(entry.getKey()).append("|")
                        .append(event).append("|")
                        .append(category).append("\n");
            }
        }
        return sb.toString();
    }

    // 문자열에서 이벤트 데이터 복원
    public void setEventsFromString(String eventsData) {
        events.clear();
        eventCategories.clear();
        categories.clear();
        categories.add("일반");

        String[] lines = eventsData.split("\n");
        for (String line : lines) {
            if (line.trim().isEmpty())
                continue;
            String[] parts = line.split("\\|");
            if (parts.length == 3) {
                events.computeIfAbsent(parts[0], k -> new ArrayList<>()).add(parts[1]);
                eventCategories.put(parts[0] + "|" + parts[1], parts[2]);
                categories.add(parts[2]);
            }
        }
        updateCategoryCheckboxes((JPanel) categoryPanel.getComponent(0));
        updateCalendar();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // 테스트용 프레임 생성
            JFrame frame = new JFrame("캘린더");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(600, 400);

            // 캘린더 패널 생성 및 프레임에 추가
            ChatCalendar calendar = new ChatCalendar();
            frame.add(calendar);

            // 테스트용 이벤트 데이터 추가
            Calendar now = Calendar.getInstance();
            String today = String.format("%d-%02d-%02d",
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH) + 1,
                    now.get(Calendar.DAY_OF_MONTH));
            calendar.events.computeIfAbsent(today, k -> new ArrayList<>()).add("테스트 이벤트");
            calendar.updateCalendar();

            // 화면 중앙에 표시
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
