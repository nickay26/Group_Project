package Project;

import java.util.List;

    public class Question {
        private int id;
        private String category;
        private String question;
        private List<String> options;
        private String answer;

        // Constructor
        public Question(int id, String category, String question, List<String> options, String answer) {
            this.id = id;
            this.category = category;
            this.question = question;
            this.options = options;
            this.answer = answer;
        }

        // Getters and Setters
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }

        public List<String> getOptions() {
            return options;
        }

        public void setOptions(List<String> options) {
            this.options = options;
        }

        public String getAnswer() {
            return answer;
        }

        public void setAnswer(String answer) {
            this.answer = answer;
        }
    }