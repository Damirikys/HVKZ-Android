package org.hvkz.hvkz.uapi.extensions;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class UserData
{
    private static final String WEIGHT = "Текущий вес";
    private static final String GROWTH = "Рост";
    private static final String DESIRED_WEIGHT = "Желаемый вес";
    private static final String WAIST_CIRCUMFERENCE = "Обхват талии";
    private static final String GIRTH_PELVIS = "Обхват таза";
    private static final String GIRTH_BUTTOCKS = "Обхват ягодиц";
    private static final String HIP_CIRCUMFERENCE = "Обхват бедра";

    private static final String CHRONIC_DISEASES = "Хронические заболевания";
    private static final String KIDNEY_DISEASES = "Заболевания почек";
    private static final String LIVER_DISEASES = "Заболевания печени";
    private static final String DIGESTIVE_TRACT_DISEASES = "Заболевания ЖКТ";
    private static final String POLYCYSTIC_OVARY = "Поликистоз яичников";
    private static final String SCOLIOSIS_KYPHOSIS_LORDOSIS = "Сколиоз, кифоз, лордоз";
    private static final String FLAT_FOOTEDNESS = "Плоскостопие";
    private static final String VEGETATIVE_DYSFUNCTION = "Вегетативная дисфункция";
    private static final String OSTEOCHONDROSIS = "Остеохондроз";
    private static final String ARTHRITIS_ARTHOSIS = "Артрит, артоз";
    private static final String INTERVERTEBRAL_HERNIAS_PROTRUSIONS = "Межпозвонковые грыжи, протрузии";
    private static final String PHLEBEURYSM = "Варикозное расширение вен";
    private static final String GASTRITIS = "Гастрит";
    private static final String HEART_PROBLEMS = "Проблемы с сердцем";
    private static final String PROBLEM_WITH_PRESSURE = "Проблемы с давлением";
    private static final String VASCULAR_PROBLEMS = "Проблемы с сосудами";

    private static final String HORMONAL_DRUGS = "Употребление гормональных препаратов";
    private static final String CONTRACEPTIVE = "Употребление противозачаточных препаратов";
    private static final String DIASTASIS = "Наличие диастаза";
    private static final String OMISSION_WALLS_VAGINA = "Опущение стенок влагалища";
    private static final String BREAST_CHILDREN = "Наличие грудных детей";
    private static final String LIFESTYLE = "Образ жизни";
    private static final String SLEEP_AMOUNT = "Время сна";
    private static final String EATING_FOOD = "Употребление пищи";
    private static final String ALCOHOL = "Употребление алкоголя";
    private static final String SMOKING = "Курение";
    private static final String FAMILY_FATTY = "В семье есть люди с избыточной массой тела";
    private static final String FAT_BURNERS = "Употребление жиросжигателей";
    private static final String SPORTS_NUTRITION = "Употребление спортпита";

    private String chest;
    private String underchest;
    private String data;

    private Map<String, String> protocol = new HashMap<String, String>() {{
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
