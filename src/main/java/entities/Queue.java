package entities;

import entities.enums.Discipline;

import javax.persistence.*;

@Entity
@Table(name = "queue")
public class Queue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "chat_id")
    private User user;

    @Column(name = "discipline")
    @Enumerated(EnumType.STRING)
    private Discipline discipline;

    @Column(name = "lab_num")
    private int labNumber;

    @Column(name = "queue_num")
    private int queueNumber;

    protected Queue() {
    }

    public Queue(User user, Discipline discipline, int labNumber, int queueNumber) {
        this.user = user;
        this.discipline = discipline;
        this.labNumber = labNumber;
        this.queueNumber = queueNumber;
    }

    // getters

    public User getUser() {
        return user;
    }

    public Discipline getDiscipline() {
        return discipline;
    }

    public int getLabNumber() {
        return labNumber;
    }

    public int getQueueNumber() {
        return queueNumber;
    }

    // core

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Queue queue = (Queue) o;

        if (id != queue.id) return false;
        if (labNumber != queue.labNumber) return false;
        if (queueNumber != queue.queueNumber) return false;
        if (!user.equals(queue.user)) return false;
        return discipline == queue.discipline;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + user.hashCode();
        result = 31 * result + discipline.hashCode();
        result = 31 * result + labNumber;
        result = 31 * result + queueNumber;
        return result;
    }

    @Override
    public String toString() {
        return "Queue{" +
                "id=" + id +
                ", user=" + user +
                ", discipline=" + discipline +
                ", labNumber=" + labNumber +
                ", queueNumber=" + queueNumber +
                '}';
    }
}
