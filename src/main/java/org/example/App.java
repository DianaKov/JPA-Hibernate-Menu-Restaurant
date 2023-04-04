package org.example;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class App {
    static EntityManagerFactory emf;
    static EntityManager em;
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        try {
            // create connection
            emf = Persistence.createEntityManagerFactory("jpaHomework2");
            em = emf.createEntityManager();
            try {
                while (true) {
                    System.out.println("1: add to menu");
                    System.out.println("2: view all menu");
                    System.out.println("3: sort by cost");
                    System.out.println("4: view only with discount");
                    System.out.println("5: choose dishes (no more than 1 kg in total)");
                    System.out.print("-> ");

                    String choice = sc.nextLine();
                    switch (choice) {
                        case "1": {
                            addDish(sc);
                            break;
                        }
                        case "2": {
                            viewAllDishes();
                            break;
                        }
                        case "3": {
                            sortByCost(sc);
                            break;
                        }
                        case "4": {
                            viewOnlyWithDiscount();
                            break;
                        }
                        case "5": {
                            chooseDishes(sc);
                            break;
                        }
                        default:
                            return;
                    }
                }
            } finally {
                sc.close();
                em.close();
                emf.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
    }

    private static void addDish(Scanner sc) {
        try {
            em.getTransaction().begin();

            System.out.print("Enter dish name: ");
            String dishName = sc.nextLine();

            System.out.print("Enter price: ");
            Double price = Double.parseDouble(sc.nextLine());

            System.out.print("Enter weight: ");
            Double weight = Double.parseDouble(sc.nextLine());

            System.out.print("Enter discount (true or false): ");
            Boolean discount = Boolean.parseBoolean(sc.nextLine());

            Menu menu = new Menu();
            menu.setDishName(dishName);
            menu.setPrice(price);
            menu.setWeight(weight);
            menu.setDiscount(discount);

            em.persist(menu);
            em.getTransaction().commit();

            System.out.println("Dish added successfully.");
        } catch (Exception ex) {
            em.getTransaction().rollback();
            System.out.println("Error: " + ex.getMessage());
        }
    }

    private static void viewAllDishes() {
        try {
            TypedQuery<Menu> query = em.createQuery("SELECT m FROM Menu m", Menu.class);
            List<Menu> menuList = query.getResultList();
            if (menuList.isEmpty()) {
                System.out.println("No dishes found.");
            } else {
                for (Menu menu : menuList) {
                    System.out.printf("%d. %s, %.2f UAN, %.2f g, discount: %s\n",
                            menu.getId(), menu.getDishName(), menu.getPrice(), menu.getWeight(), menu.getDiscount());
                }
            }
        } catch (Exception ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }

    private static void sortByCost(Scanner sc) {
        System.out.print("Enter minimum menu price: ");
        double minPrice = Float.parseFloat(sc.nextLine());
        System.out.print("Enter maximum menu price: ");
        double maxPrice = Float.parseFloat(sc.nextLine());

        Query query = em.createQuery("SELECT a FROM Menu a WHERE a.price >= :minPrice AND a.price <= :maxPrice ORDER BY a.price ASC", Menu.class);
        query.setParameter("minPrice", minPrice);
        query.setParameter("maxPrice", maxPrice);

        List<Menu> list = (List<Menu>) query.getResultList();
        for (Menu a : list) {
            System.out.println(a);
        }
    }
    private static void viewOnlyWithDiscount() {
        Query query = em.createQuery("SELECT a FROM Menu a WHERE a.discount = true", Menu.class);
        List<Menu> list = (List<Menu>) query.getResultList();
        for (Menu a : list) {
            System.out.println(a);
        }
    }

    private static void chooseDishes(Scanner sc) {
        System.out.println("Menu:");
        viewAllDishes();
        long totalWeight = 0;
        List<Long> chosenDishes = new ArrayList<>();

        while (true) {
            System.out.printf("Total weight of chosen dishes: %.2f kg\n", totalWeight / 1000.0);
            System.out.println("Enter dish number to choose or '0' to exit:");
            long dishId = sc.nextLong();

            if (dishId == 0) {
                showChosenDishes(chosenDishes);
                break;
            }

            Menu chosenDish = em.find(Menu.class, dishId);
            if (chosenDish == null) {
                System.out.println("Dish not found.");
                continue;
            }

            if (chosenDish.getWeight() < 1000 && totalWeight + chosenDish.getWeight() > 1000) {
                System.out.println("Total weight of chosen dishes exceeds 1 kg.");
                continue;
            }

            chosenDishes.add(dishId);
            totalWeight += chosenDish.getWeight();

            System.out.printf("%s added to the order. Total weight: %.2f kg\n",
                    chosenDish.getDishName(), totalWeight / 1000.0);
        }

        System.out.printf("Total weight of chosen dishes: %.2f kg\n", totalWeight / 1000.0);

        if (chosenDishes.isEmpty()) {
            System.out.println("No dishes were chosen.");
        }
    }

    private static void showChosenDishes(List<Long> chosenDishes) {
        double totalPrice = 0;
        System.out.println("Chosen dishes:");
        for (Long dishId : chosenDishes) {
            Menu chosenDish = em.find(Menu.class, dishId);
            System.out.printf("%d. %s, %.2f UAN, %.2f kg, discount: %s\n",
                    chosenDish.getId(), chosenDish.getDishName(), chosenDish.getPrice(),
                    chosenDish.getWeight(), chosenDish.getDiscount());
            totalPrice += chosenDish.getPrice();
        }
        System.out.printf("Total price of chosen dishes: %.2f UAN\n", totalPrice);
    }
}


