package cs492.pod.model;

public enum DrugCategory {
  //@formatter:off
  Type1("alprazolam", "niravam", "xanax"),
  Type2("ibuprofen", "advil", "genpril", "motrin", "midol", "nuprin"),
  Type3("omeprazole", "prilosec"),
  Type4("metformin", "glucophage", "glumetza", "sulfonylurea"),
  Type5("levothyroxine", "tirosint"),
  Type6("metronidazole", "flagyl")
  ;
  //@formatter:on

  private final String[] drugs;

  DrugCategory(String... drugs) {
    this.drugs = drugs;
  }

  public String[] getDrugs() {
    return drugs;
  }

  public static DrugCategory getDrugCategory(String drug) {
    for (DrugCategory dc : DrugCategory.values()) {
      for (String d : dc.getDrugs()) {
        if (drug.trim().toLowerCase().equals(d)) {
          return dc;
        }
      }
    }
    return null;
  }

  public static DrugCategory getByOrdinal(int ordinal) {
    for (DrugCategory dc : DrugCategory.values()) {
      if (dc.ordinal() == ordinal) {
        return dc;
      }
    }
    return null;
  }
}
