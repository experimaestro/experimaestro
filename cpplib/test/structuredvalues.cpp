//
// Created by Benjamin Piwowarski on 13/12/2016.
//

#include <xpm/xpm.hpp>
#include <gtest/gtest.h>

//using nlohmann::json;
using namespace xpm;

struct TestType {
  Type type;
  TestType() : type(TypeName("test")) {
    auto a = std::make_shared<Argument>("a");
    a->defaultValue(Value::create(1l));
    type.addArgument(a);
  }
};

TEST(StructuredValue, defaultSet) {
  auto object = TestType().type.create();
  object->set("a", Value(1l));
  object->validate();

  EXPECT_TRUE(object->get("a")->equals(Value(1)));
  EXPECT_TRUE(object->get("a")->isDefault());
}

TEST(StructuredValue, notDefault) {
  auto object = TestType().type.create();
  object->set("a", Value(2));
  object->validate();

  EXPECT_TRUE(object->get("a")->equals(Value(2)));
  EXPECT_TRUE(!object->get("a")->isDefault());
}


TEST(StructuredValue, defaultNotSet) {
  auto object = TestType().type.create();
  object->validate();

  EXPECT_TRUE(object->get("a")->equals(Value(1)));
  EXPECT_TRUE(object->get("a")->isDefault());
}
