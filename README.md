# onto-module-metrics

Provides several implementations of the [cohesion](https://en.wikipedia.org/wiki/Cohesion_(computer_science)) and [coupling](https://en.wikipedia.org/wiki/Coupling_(computer_programming)) metrics for [OWL](https://www.w3.org/TR/owl2-overview/) and [AspectOWL](https://aspectowl.xyz) ontology modules.

The current version includes implementations of both of the above metrics as described by

- Oh et al. in [[1]](#user-content-oh) and
- Kumar et al. in [[2]](#user-content-kumar).

<a name="oh">[1]</a> Oh, Sunju, Heon Y. Yeom, and Joongho Ahn. 2011. “Cohesion and Coupling Metrics for Ontology Modules.” Information Technology and Management 12 (2): 81–96. https://doi.org/10.1007/s10799-011-0094-5

<a name="kumar">[2]</a> Kumar, Sandeep, Niyati Baliyan, and Shriya Sukalikar. 2017. “Ontology Cohesion and Coupling Metrics.” International Journal on Semantic Web and Information Systems (IJSWIS) 13 (4): 1–26. https://doi.org/10.4018/IJSWIS.2017100101

## Usage

### Installation

onto-module-metrics is a java library.

Add the library as a Maven dependency to your project.

```
<dependency>
    <groupId>xyz.aspectowl</groupId>
    <artifactId>onto-module-metrics</artifactId>
    <version>0.0.2-SNAPSHOT</version>
</dependency>
```

### Adding ontology modules

First, create or load an ontology the usual way using the [OWLAPI](https://github.com/owlcs/owlapi), for example:

```java
OWLOntologyManager om = OWLManager.createOWLOntologyManager();
OWLOntology onto = om.loadOntologyFromOntologyDocument(file);
```

Each metric is implemented by a class extending the abstract class ```CohesionCouplingMetric```. Depending on which metric you wish to use, create an instance of the corresponding class.

```java
CohesionCouplingMetric metric = new Oh2011();
```

or

```java
CohesionCouplingMetric metric = new Kumar2017()
```

If your ontology is a module of a larger ontology, add it (along with every other module of the larger ontology) using the ```addModule``` method:

```java
metric.addModule(onto);
```

Alternatively, you can add an entire modularized ontology at one time using the ```setModularizedOntology``` method. In that case, each ontology in the imports closure of the given ontology will be treated as a module.

For example, of the ontology ```onto``` imports the three ontology modules ```o1```, ```o2```, and ```o3```, then ```onto``` itself, ```o1```, ```o2```, and ```o3``` will be added as modules.

```
metric.setModularizedOntology(onto);
```

### Getting cohesion and coupling values

In order to retrieve the calculated cohesion and coupling values for a specifiv module, use the ```getCohesion``` and ```getCoupling``` methods.

For example:

```java
onto.getOWLOntologyManager()
        .importsClosure(onto)
        .forEach(
            module -> {
                double cohesion = metric.getCohesion(module);
                double coupling = metric.getCoupling(module);
            });
```
