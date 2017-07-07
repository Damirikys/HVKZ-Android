package org.hvkz.hvkz.uapi.models.entities;

import java.util.HashMap;
import java.util.Map;

public class UserData
{
    public static final String WEIGHT = "Текущий вес";
    public static final String GROWTH = "Рост";
    public static final String DESIRED_WEIGHT = "Желаемый вес";
    public static final String WAIST_CIRCUMFERENCE = "Обхват талии";
    public static final String GIRTH_PELVIS = "Обхват таза";
    public static final String GIRTH_BUTTOCKS = "Обхват ягодиц";
    public static final String HIP_CIRCUMFERENCE = "Обхват бедра";

    public static final String CHRONIC_DISEASES = "Хронические заболевания";
    public static final String KIDNEY_DISEASES = "Заболевания почек";
    public static final String LIVER_DISEASES = "Заболевания печени";
    public static final String DIGESTIVE_TRACT_DISEASES = "Заболевания ЖКТ";
    public static final String POLYCYSTIC_OVARY = "Поликистоз яичников";
    public static final String SCOLIOSIS_KYPHOSIS_LORDOSIS = "Сколиоз, кифоз, лордоз";
    public static final String FLAT_FOOTEDNESS = "Плоскостопие";
    public static final String VEGETATIVE_DYSFUNCTION = "Вегетативная дисфункция";
    public static final String OSTEOCHONDROSIS = "Остеохондроз";
    public static final String ARTHRITIS_ARTHOSIS = "Артрит, артоз";
    public static final String INTERVERTEBRAL_HERNIAS_PROTRUSIONS = "Межпозвонковые грыжи, протрузии";
    public static final String PHLEBEURYSM = "Варикозное расширение вен";
    public static final String GASTRITIS = "Гастрит";
    public static final String HEART_PROBLEMS = "Проблемы с сердцем";
    public static final String PROBLEM_WITH_PRESSURE = "Проблемы с давлением";
    public static final String VASCULAR_PROBLEMS = "Проблемы с сосудами";

    public static final String HORMONAL_DRUGS = "Употребление гормональных препаратов";
    public static final String CONTRACEPTIVE = "Употребление противозачаточных препаратов";
    public static final String DIASTASIS = "Наличие диастаза";
    public static final String OMISSION_WALLS_VAGINA = "Опущение стенок влагалища";
    public static final String BREAST_CHILDREN = "Наличие грудных детей";
    public static final String LIFESTYLE = "Образ жизни";
    public static final String SLEEP_AMOUNT = "Время сна";
    public static final String EATING_FOOD = "Употребление пищи";
    public static final String ALCOHOL = "Употребление алкоголя";
    public static final String SMOKING = "Курение";
    public static final String FAMILY_FATTY = "В семье есть люди с избыточной массой тела";
    public static final String FAT_BURNERS = "Употребление жиросжигателей";
    public static final String SPORTS_NUTRITION = "Употребление спортпита";

    private String chest;
    private String underchest;
    private String data;

    public Map<String, String> protocol = new HashMap<String, String>() {{
       put(DESIRED_WEIGHT, "P_A");
       put(WEIGHT, "P_V");
       put(GROWTH, "P_0");
       put(WAIST_CIRCUMFERENCE, "P_1");
       put(GIRTH_PELVIS, "P_2");
       put(GIRTH_BUTTOCKS, "P_3");
       put(HIP_CIRCUMFERENCE, "P_4");
       put(CHRONIC_DISEASES, "P_5");
       put(KIDNEY_DISEASES, "P_6");
       put(LIVER_DISEASES, "P_7");
       put(DIGESTIVE_TRACT_DISEASES, "P_8");
       put(POLYCYSTIC_OVARY, "P_9");
       put(SCOLIOSIS_KYPHOSIS_LORDOSIS, "P_10");
       put(FLAT_FOOTEDNESS, "P_11");
       put(VEGETATIVE_DYSFUNCTION, "P_12");
       put(OSTEOCHONDROSIS, "P_13");
       put(ARTHRITIS_ARTHOSIS, "P_14");
       put(INTERVERTEBRAL_HERNIAS_PROTRUSIONS, "P_15");
       put(PHLEBEURYSM, "P_16");
       put(GASTRITIS, "P_17");
       put(HEART_PROBLEMS, "P_18");
       put(PROBLEM_WITH_PRESSURE, "P_19");
       put(VASCULAR_PROBLEMS, "P_20");
       put(HORMONAL_DRUGS, "P_21");
       put(CONTRACEPTIVE, "P_22");
       put(DIASTASIS, "P_23");
       put(OMISSION_WALLS_VAGINA, "P_24");
       put(BREAST_CHILDREN, "P_25");
       put(LIFESTYLE, "P_26");
       put(SLEEP_AMOUNT, "P_27");
       put(EATING_FOOD, "P_28");
       put(ALCOHOL, "P_29");
       put(SMOKING, "P_30");
       put(FAMILY_FATTY, "P_31");
       put(FAT_BURNERS, "P_32");
       put(SPORTS_NUTRITION, "P_33");
    }};

    public UserData(String data, String chest, String underchest) {
        this.data = data;
        this.chest = chest;
        this.underchest = underchest;
    }

    public String getField(String field) {
        return fieldExtract(field);
    }

    public String getChestCircumference() {
        return chest;
    }

    public String getUnderChestCircumference() {
        return underchest;
    }

    private String fieldExtract(String field) {
        String key = protocol.get(field);
        int index = data.indexOf(key) + key.length();
        StringBuilder builder = new StringBuilder();
        while (data.charAt(index) != 'P') {
            builder.append(data.charAt(index));
            index++;
        }

        return builder.toString().trim();
    }
}
